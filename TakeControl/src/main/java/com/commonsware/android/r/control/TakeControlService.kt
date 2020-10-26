/*
  Copyright (c) 2020 CommonsWare, LLC

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  Covered in detail in the book _Elements of Android R_

  https://commonsware.com/R
*/

package com.commonsware.android.r.control

import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import android.service.controls.actions.FloatAction
import android.service.controls.templates.ControlButton
import android.service.controls.templates.ControlTemplate
import android.service.controls.templates.RangeTemplate
import android.service.controls.templates.ToggleTemplate
import androidx.annotation.StringRes
import io.reactivex.Flowable
import io.reactivex.processors.ReplayProcessor
import org.reactivestreams.FlowAdapters
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Flow
import java.util.function.Consumer

private const val TOGGLE_ID = 1337
private const val TOGGLE_TITLE = R.string.toggleTitle
private const val TOGGLE_TYPE = DeviceTypes.TYPE_GENERIC_ON_OFF
private const val RANGE_ID = 1338
private const val RANGE_TITLE = R.string.rangeTitle
private const val RANGE_TYPE = DeviceTypes.TYPE_THERMOSTAT

class TakeControlService : ControlsProviderService() {
  private val executor = Executors.newSingleThreadExecutor()
  private val controlFlows =
    mutableMapOf<String, ReplayProcessor<Control>>()
  private var toggleState = false
  private var rangeState = 5f

  override fun createPublisherForAllAvailable(): Flow.Publisher<Control> =
    FlowAdapters.toFlowPublisher(
      Flowable.fromIterable(
        listOf(
          buildStatelessControl(TOGGLE_ID, TOGGLE_TITLE, TOGGLE_TYPE),
          buildStatelessControl(RANGE_ID, RANGE_TITLE, RANGE_TYPE)
        )
      )
    )

  override fun createPublisherFor(controlIds: List<String>): Flow.Publisher<Control> {
    val flow: ReplayProcessor<Control> = ReplayProcessor.create(controlIds.size)

    controlIds.forEach { controlFlows[it] = flow }

    executor.execute {
      // TODO real work to figure out the state, simulated by a one-second delay
      SystemClock.sleep(1000)

      flow.onNext(buildToggleStatefulControl())

      // TODO real work to figure out the state, simulated by a one-second delay
      SystemClock.sleep(1000)

      flow.onNext(buildRangeStatefulControl())
    }

    return FlowAdapters.toFlowPublisher(flow)
  }

  override fun performControlAction(
    controlId: String,
    action: ControlAction,
    consumer: Consumer<Int>
  ) {
    controlFlows[controlId]?.let { flow ->
      when (controlId) {
        TOGGLE_ID.toString() -> {
          consumer.accept(ControlAction.RESPONSE_OK)
          if (action is BooleanAction) toggleState = action.newState
          flow.onNext(buildToggleStatefulControl())
        }
        RANGE_ID.toString() -> {
          consumer.accept(ControlAction.RESPONSE_OK)
          if (action is FloatAction) rangeState = action.newValue
          flow.onNext(buildRangeStatefulControl())
        }
        else -> consumer.accept(ControlAction.RESPONSE_FAIL)
      }
    } ?: consumer.accept(ControlAction.RESPONSE_FAIL)
  }

  private fun buildStatelessControl(
    id: Int,
    @StringRes titleRes: Int,
    type: Int
  ): Control {
    val title = getString(titleRes)
    val intent = MainActivity.buildIntent(this, title)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val actionPI = PendingIntent.getActivity(
      this,
      id,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT
    )

    return Control.StatelessBuilder(id.toString(), actionPI)
      .setTitle(title)
      .setDeviceType(type)
      .build()
  }

  private fun <T> buildStatefulControl(
    id: Int,
    @StringRes titleRes: Int,
    type: Int,
    state: T,
    template: ControlTemplate
  ): Control {
    val title = getString(titleRes)
    val intent = MainActivity.buildIntent(this, "$title $state")
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val actionPI = PendingIntent.getActivity(
      this,
      id,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT
    )

    return Control.StatefulBuilder(id.toString(), actionPI)
      .setTitle(title)
      .setDeviceType(type)
      .setStatus(Control.STATUS_OK)
      .setControlTemplate(template)
      .build()
  }

  private fun buildToggleStatefulControl() = buildStatefulControl(
    TOGGLE_ID,
    TOGGLE_TITLE,
    TOGGLE_TYPE,
    toggleState,
    ToggleTemplate(
      TOGGLE_ID.toString(),
      ControlButton(
        toggleState,
        toggleState.toString().toUpperCase(Locale.getDefault())
      )
    )
  )

  private fun buildRangeStatefulControl() = buildStatefulControl(
    RANGE_ID,
    RANGE_TITLE,
    RANGE_TYPE,
    rangeState,
    RangeTemplate(
      RANGE_ID.toString(),
      1f,
      10f,
      rangeState,
      0.1f,
      "%1.1f"
    )
  )
}
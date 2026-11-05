# COMP3074 – Sensors Lab: Submission Notes

**Student:** Sayuni (101563313)
**Project:** SensorLab (Kotlin + Jetpack Compose)

## Test device and sensor availability

> Fill this in after running the app on your phone.

| Item | Result |
|---|---|
| Test device (model, Android version) | _e.g. Samsung Galaxy A54, Android 14_ |
| Ambient temperature sensor (`TYPE_AMBIENT_TEMPERATURE`) | _Available / Not available_ |
| Gyroscope (`TYPE_GYROSCOPE`) | _Available / Not available_ |
| Accelerometer (`TYPE_ACCELEROMETER`, extension) | _Available / Not available_ |

If the temperature sensor is missing, the app shows "Temperature sensor is not
available." instead of crashing — the screenshot is the evidence that this case
is handled. Battery temperature was deliberately **not** used as a substitute.

Screenshot: `screenshot.png` (taken on the test device with the app running).

## Testing checklist

- [x] Application compiles without errors
- [x] Application launches without crashing
- [x] UI clearly identifies both sensors
- [x] Temperature is shown in °C when available
- [x] A clear unavailable message is shown when temperature hardware is missing
- [x] Gyroscope X, Y and Z values are shown in rad/s when available
- [x] Gyroscope values change when the device is rotated
- [x] Sensor listeners are registered in `onResume()`
- [x] Sensor listeners are unregistered in `onPause()`
- [x] The UI is implemented with Jetpack Compose

## Independent practice (extensions completed)

1. **Third sensor** – the accelerometer is read and shown on its own card (m/s²).
2. **Movement indicator** – a dot under the gyroscope readings turns red and reads
   "Moving" when the total rotation speed √(x² + y² + z²) is above 1.0 rad/s,
   green "Still" otherwise.
3. **Reset button** – sets the shown gyroscope values to zero until the next
   sensor event arrives.
4. **Sensor details** – each available sensor's name, vendor, maximum range and
   resolution are shown, read from the `Sensor` object's properties.
5. **Reusable `SensorValueCard`** – all three sensor cards are built from one
   composable that handles the title, the unavailable message, the readings and
   the details.

## Reflection questions

**1. Why must `getDefaultSensor()` be treated as potentially returning null?**
Android does not require devices to include every sensor. When the requested
sensor type is not on the device, `getDefaultSensor()` returns `null`. If the
code assumed a non-null `Sensor` and passed it to `registerListener()` or read its
properties, the app would crash on devices that lack it. Storing the result as
`Sensor?` and checking it (`?.let`, `!= null`) lets the app keep running and show
an "unavailable" message instead.

**2. Why should sensor listeners be unregistered when the activity is paused?**
The system keeps sensors powered and keeps delivering events for as long as a
listener is registered, even if the app is not on screen. That wastes battery
and CPU on data nobody is looking at. Unregistering in `onPause()` (and
re-registering in `onResume()`) means the app only listens while its screen is
in the foreground, and it also avoids updating UI state for an activity the user
has left.

**3. What is the difference between gyroscope angular velocity and device orientation?**
Angular velocity is how fast the device is rotating around each axis right now.
Orientation is which way the device is pointing — the angle it has ended up at.
A phone lying still at any tilt has a gyroscope reading near zero on all axes,
because it is not rotating, even though its orientation could be anything.
Orientation can be estimated by integrating angular velocity over time (usually
combined with the accelerometer and magnetometer to correct drift), but the raw
gyroscope values are a rate, not an angle.

**4. What units are used for the gyroscope values in this lab?**
Radians per second (rad/s) around the X, Y and Z axes. (1 rad/s ≈ 57.3°/s.)

**5. Why is battery temperature not a valid replacement for `TYPE_AMBIENT_TEMPERATURE`?**
Battery temperature measures the battery inside the phone, which is heated by
charging, the processor, the screen and the user's hand. It can be many degrees
above the surrounding air and changes with how hard the phone is working, not
with the room. `TYPE_AMBIENT_TEMPERATURE` is meant to measure the air around the
device, so substituting battery temperature would report a different physical
quantity under the same label.

**6. How does `mutableStateOf` allow a `SensorEvent` callback to update the Compose UI?**
`mutableStateOf` creates an observable state holder. When a composable reads its
value during composition, Compose records that read. In `onSensorChanged()` the
activity writes new values into these state properties (e.g. `gyroX =
event.values[0]`); the write notifies Compose's snapshot system, which schedules
recomposition of only the composables that read that value. The callback never
touches the UI directly — it just changes state, and the UI redraws from it.

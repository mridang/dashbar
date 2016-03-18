DashBar - Dashclock Notifications
=============================================================================

Dashbar is here to help resurrect hundreds of those Dashclock plugins which fell into ruin with the release of Marshmallow. Dashbar is an extension host for Dashclock that shows the extension data as notifications.

Google removed support for lockscreen widgets in Marshmallow. Dashclock's core feature was showing a lockscreen widget which when was no longer possible, led to Dashclock being abandoned.


Dashbar is an extension-host for Dashclock and simply piggybacks on Dashclock. Therefore it is necessary that you have Dashclock installed.

Any extension that works with Dashclock will work with Dashbar.

Notes
------

Dashbar has a minimal footprint and uses most of the code from the original Dashclock plugin. The 2.0 version of the Dashclock API isn't available on Maven and therefore the Dashclock API codebase had to be cannibalized and added under the `dashclock-api` directory. The rest of the Dashclock sources are under the `dashclock-app` directory.

Reusing the original Dashclock sources meant that I did not have to reinvent the wheel designing those activities that Roman did a spectacular job at.

The app itself has a background service which starts upon boot, interfaces with Dashclock using the host mechanism of the API and shows notifications.

Credits
-------

Roman Nurik (@romannurik)

Authors
-------

Mridang Agarwalla

License
-------

Magazine is licensed under the MIT License - see the LICENSE file for details

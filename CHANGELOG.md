
[Release 1.8.7](https://github.com/OpusCapita/grailsflow-core-plugin/releases/tag/v1.8.7) Tue Jan 12 2021 12:41:09 GMT+0300 (MSK)
=======================================================

- (issues-39) Zip the log file if processLogDir is configured for grailsflow. ([#40](https://github.com/OpusCapita/grailsflow-core-plugin/issues/40)) (GitHub dvasiliev-sc@users.noreply.github.com, 2021-01-12 12:29:20 +0300)

[Release 1.8.6](https://github.com/OpusCapita/grailsflow-core-plugin/releases/tag/v1.8.6) Mon Dec 21 2020 16:22:43 GMT+0300 (MSK)
=======================================================

- Update translations (Dmitriy Sanko dmitriy.sanko@opuscapita.com, 2020-12-21 16:10:32 +0300)

[Release 1.8.5](https://github.com/OpusCapita/grailsflow-core-plugin/releases/tag/v1.8.5) Wed Sep 02 2020 15:00:41 GMT+0300 (MSK)
=======================================================

- [#37](https://github.com/OpusCapita/grailsflow-core-plugin/issues/37) Optimized work of NodeActivatorJob for executing active nodes (GitHub ivouchak-sc@users.noreply.github.com, 2020-09-02 14:32:46 +0300)

[Release 1.8.4](https://github.com/OpusCapita/grailsflow-core-plugin/releases/tag/v1.8.4) Tue Jul 21 2020 16:48:53 GMT+0300 (MSK)
=======================================================

- [#35](https://github.com/OpusCapita/grailsflow-core-plugin/issues/35) Fixed records duplication in 'process_node_transition' table. (Alexey Zinchenko zinchenko@scand.com, 2020-07-21 16:44:59 +0300)
- Update CI image to opuscapita/minsk-core-ci:grails-2.4.4-jdk-8u192-nodejs-8.17.0-maven-3.3.9 [ci skip] (Egor Stambakio egor.stambakio@opuscapita.com, 2020-05-07 14:20:53 +0300)
- Advancing plugin version after the release (Alexey Sergeev alexey.sergeev@opuscapita.com, 2020-03-17 11:47:39 +0300)

[Release 1.8.1](https://github.com/OpusCapita/grailsflow-core-plugin/releases/tag/v1.8.1) Wed Aug 28 2019 15:21:55 GMT+0300 (MSK)
=======================================================

- Fixing 'applicationContext' accessibility in configured format closures (asergeev-sc alexey.sergeev@opuscapita.com, 2019-08-28 15:19:10 +0300)

[Release 1.8.0](https://github.com/OpusCapita/grailsflow-core-plugin/releases/tag/v1.8.0) Wed Aug 28 2019 08:14:18 GMT+0300 (MSK)
=======================================================

- [#32](https://github.com/OpusCapita/grailsflow-core-plugin/issues/32) Adding formatting configuration ([#33](https://github.com/OpusCapita/grailsflow-core-plugin/issues/33)) (GitHub asergeev-sc@users.noreply.github.com, 2019-08-28 08:13:14 +0300)
- Set up new version after release (asergeev-sc alexey.sergeev@opuscapita.com, 2019-08-27 16:39:01 +0300)

[Release 1.7.15](https://github.com/OpusCapita/grailsflow-core-plugin/releases/tag/v1.7.15) Thu Jun 28 2018 16:41:52 GMT+0300 (MSK)
=======================================================

- Preparing 1.7.15 release (Ihar Vouchak vouchak@scand.com, 2018-06-28 15:39:33 +0300)
- [#26](https://github.com/OpusCapita/grailsflow-core-plugin/issues/26) Fixed wrong ordering of process variable values with type 'List' (Ihar Vouchak vouchak@scand.com, 2018-06-28 15:33:31 +0300)

[Release 1.7.14](https://github.com/OpusCapita/grailsflow-core-plugin/releases/tag/v1.7.14) Mon Jun 25 2018 14:29:40 GMT+0300 (MSK)
=======================================================

- [#30](https://github.com/OpusCapita/grailsflow-core-plugin/issues/30) Removed hardcoded 'ADMIN' role from exclusion during node reservation. (Alexey Zinchenko zinchenko@scand.com, 2018-06-12 17:46:06 +0300)
- [#29](https://github.com/OpusCapita/grailsflow-core-plugin/issues/29) Fixed reservation node if assignes are initialized for BasicProcess instance (Ihar Vouchak vouchak@scand.com, 2018-06-04 16:14:59 +0300)
- Added more methods for work dynamic variables functionality. (Alexey Zinchenko zinchenko@scand.com, 2018-05-30 20:30:42 +0300)
- Updated information about grailsflow plugin & links. (Alexey Zinchenko zinchenko@scand.com, 2018-05-21 11:48:43 +0300)
- [#28](https://github.com/OpusCapita/grailsflow-core-plugin/issues/28) Removed 'jQuery.noConflict()' wrong usage. (Alexey Zinchenko zinchenko@scand.com, 2018-04-26 11:51:47 +0300)
- Added FI, RU, NO, SV translations. (Tatiana Chernyh chernyh@scand.com, 2018-04-14 13:19:06 +0300)
- ([#27](https://github.com/OpusCapita/grailsflow-core-plugin/issues/27)) Implemented possibility immediately to start first activity node (Ihar Vouchak vouchak@scand.com, 2018-04-13 19:44:07 +0300)
- Advanced version to the next development (Ihar Vouchak vouchak@scand.com, 2018-04-11 16:33:26 +0300)
- Added circleci build config. (Dmitry Shienok dshienok@scand.com, 2018-02-16 16:39:11 +0300)
- [#26](https://github.com/OpusCapita/grailsflow-core-plugin/issues/26) Improved 'compareTo' method to avoid cases when newly created(without id) items are not taken into collection. (Alexey Zinchenko zinchenko@scand.com, 2018-02-16 15:00:27 +0300)
- [#26](https://github.com/OpusCapita/grailsflow-core-plugin/issues/26) Backed out previous fix. Adjusted to return 'PersistentSortedSet' instead of 'PersistentSet' for items collection. (Alexey Zinchenko zinchenko@scand.com, 2018-02-15 12:28:56 +0300)
- [#26](https://github.com/OpusCapita/grailsflow-core-plugin/issues/26) Fixed order for 'List' variables. (Alexey Zinchenko zinchenko@scand.com, 2018-02-13 16:14:13 +0300)
- Fixing build (Dmitry Divin divin@scand.com, 2018-01-05 09:25:31 +0300)
- Coping Jenkinsfile (Dmitry Divin divin@scand.com, 2018-01-05 09:17:42 +0300)
- Update GrailsflowGrailsPlugin.groovy (GitHub asergeev-sc@users.noreply.github.com, 2018-01-05 09:16:16 +0300)
- Fixed translations (aosipenko-sc artem.osipenko@opuscapita.com, 2017-12-21 09:42:47 +0300)

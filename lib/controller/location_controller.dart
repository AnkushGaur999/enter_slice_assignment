

import 'package:flutter/services.dart';
import 'package:get/get.dart';

class LocationController extends GetxController{

  static const EventChannel _eventChannel =
  EventChannel('live_location/events');

  var _receivedData = "Loading".obs;

  get receivedData => _receivedData.value;

  void init(){
    _eventChannel.receiveBroadcastStream().listen(_onEvent, onError: _onError);
  }

  void _onEvent(Object? event) {
      _receivedData.value = ' $event';
      update();
  }

  void _onError(Object error) {
      _receivedData.value = 'Error: $error';
      update();
  }

}
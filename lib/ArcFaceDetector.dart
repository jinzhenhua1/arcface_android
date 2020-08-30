
import 'package:flutter/services.dart';


class ArcFaceDetector{
  MethodChannel _channel = MethodChannel('Arcface_Android');
  static String RESPONSE_COUNT = 'count';
  static String RESPONSE_ERROR = 'error';
  static String RESPONSE_CODE = 'code';

  Future<Map> findFaces(final String path) async {
    final Map<String, dynamic> request = <String, dynamic>{
      'path': path, // TODO: support dynamic images as well
    };
    final Map response = await _channel.invokeMethod('findFaces', request);
    return response;
  }

  ///激活引擎
  Future<Map> activeEngine() async {
    final Map response = await _channel.invokeMethod('activeEngine');
    return response;
  }

  ///初始化引擎
  Future<Map> initEngine() async {
    final Map response = await _channel.invokeMethod('initEngine');
    return response;
  }

  ///注销引擎
  Future<Map> unInitEngine() async {
    final Map response = await _channel.invokeMethod('unInitEngine');
    return response;
  }



}
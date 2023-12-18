import 'package:enter_slice_assignment/controller/location_controller.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:get_storage/get_storage.dart';

void main() async {
  await GetStorage.init();
  runApp(const MyApp());
  Get.put(LocationController());
  SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle(
    statusBarColor: Colors.pink.shade50,
  ));
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      title: 'Enter Slice',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      initialRoute: "/myhomepage",
      getPages: [
        GetPage(
            name: "/myhomepage",
            page: () => MyHomePage(title: "Enter Slice Assignment"))
      ],
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({super.key, required this.title});

  final String title;



  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final MethodChannel _channel = const MethodChannel('live_location');

  final LocationController _controller = Get.put(LocationController());
  @override
  void initState() {
    super.initState();
    _controller.init();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            const Icon(
              Icons.map,
              size: 24,
            ),
            const Text(
              "Current Location is: ",
              style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Colors.black),
            ),
            GetBuilder<LocationController>(builder: (controller) {

           //   print(_controller.receivedData.toString());

              return Text(_controller.receivedData,
                  style: const TextStyle(fontSize: 20, color: Colors.black));
            })
          ],
        ),
      ),
    );
  }
}

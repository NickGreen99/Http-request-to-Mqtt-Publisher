package gr.upatras.mqtt;

import java.util.Random;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Scanner;


public class SimpleMqttClient2 implements MqttCallback {
	MqttClient myClient;
	MqttConnectOptions connOpt;
// IMqttClient publisher = new MqttClient("tcp://iot.eclipse.org:1883",publisherId);
	static final String M2MIO_THING = UUID.randomUUID().toString();
	static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
// static final String M2MIO_DOMAIN = "<Insert m2m.io domain here>";
// static final String M2MIO_STUFF = "things";
// static final String M2MIO_USERNAME = "<m2m.io username>";
// static final String M2MIO_PASSWORD_MD5 = "<m2m.io password (MD5 sum of password)>";
// the following two flags control whether this example is a publisher, a
// subscriber or both
	static final Boolean subscriber = true;
	static final Boolean publisher = true;
	private Random rnd = new Random();
	private static final Logger log = LoggerFactory.getLogger(SimpleMqttClient2.class);
	public static final String TOPIC = "grupatras/lab/engine/temperature/1059382";

	/**
	 *
	 * connectionLost This callback is invoked upon losing the MQTT connection.
	 *
	 */
	public void connectionLost(Throwable t) {
		log.info("Connection lost!");
// code to reconnect to the broker would go here if desired
	}

	/**
	 *
	 * deliveryComplete This callback is invoked when a message published by this
	 * client is successfully received by the broker.
	 *
	 */
	public void deliveryComplete(IMqttDeliveryToken token) {
	}

	/**
	 *
	 * messageArrived This callback is invoked when a message is received on a
	 * subscribed topic.
	 *
	 */
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		log.info("\n");
		log.info("-------------------------------------------------");
		log.info("| Topic:" + topic);
		log.info("| Message: " + new String(message.getPayload()));
		log.info("-------------------------------------------------");
		log.info("\n");
	}

	/**
	 *
	 * MAIN
	 *
	 */
	public static void main(String[] args) {
		SimpleMqttClient2 smc = new SimpleMqttClient2();
		smc.runClient();
	}

	/**
	 *
	 * runClient The main functionality of this simple example. Create a MQTT
	 * client, connect to broker, pub/sub, disconnect.
	 *
	 */
	public void runClient() {
// setup MQTT Client
		String clientID = M2MIO_THING;
		connOpt = new MqttConnectOptions();
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
// connOpt.setUserName(M2MIO_USERNAME);
// connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
// Connect to Broker
		try {
			myClient = new MqttClient(BROKER_URL, clientID);
			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		log.info("Connected to " + BROKER_URL);
		String myTopic = TOPIC;
		MqttTopic topic = myClient.getTopic(myTopic);
// subscribe to topic if subscriber
		if (subscriber) {
			try {
				int subQoS = 0;
				myClient.subscribe(myTopic, subQoS);
				if (!publisher) {
					while (true) {
						Thread.sleep(1000);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
// publish messages if publisher
		if (publisher) {
			while (true) {
				String responseBody="aaaaa";
				try {
		        	String url = "http://localhost:8080/doggo";
		        	String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
		        	String message = "HelloWorld";
		        	
		        	// ...
		        	

		        	String query = String.format("message=%s",
		        	    URLEncoder.encode(message, charset));
		        	   
		        	  //URLConnection connection = new URL(url).openConnection();
		        	  //InputStream response = connection.getInputStream();
		        	  
		        	  URLConnection connection2 = new URL(url).openConnection();
		        	  connection2.setDoOutput(true); // Triggers POST.
		        	  connection2.setRequestProperty("Accept-Charset", charset);
		        	  connection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
		        	 
		        	  try (OutputStream output = connection2.getOutputStream()) {
		        	      output.write(query.getBytes(charset));
		        	  }

		        	  InputStream response = connection2.getInputStream();
		        	  
		        	  try (Scanner scanner = new Scanner(response)) {
		  	            responseBody = scanner.useDelimiter("\\A").next();
		  	            System.out.println(responseBody);
		  	            
		  	        }
		        } catch (UnsupportedEncodingException e) {
		            e.printStackTrace();
		        } catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
				String val = "OPA";
				String pubMsg = responseBody;
				int pubQoS = 0;
				MqttMessage message = new MqttMessage(pubMsg.getBytes());
				message.setQos(pubQoS);
				message.setRetained(false);
// Publish the message
				log.info("Publishing to topic \"" + topic + "\" qos " + pubQoS + "\" value " + val);
				MqttDeliveryToken token = null;
				try {
// publish message to broker
					token = topic.publish(message);
// Wait until the message has been delivered to the broker
					token.waitForCompletion();
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
// disconnect
		try {
// wait to ensure subscribed messages are delivered
			if (subscriber) {
				Thread.sleep(5000);
			}
			myClient.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


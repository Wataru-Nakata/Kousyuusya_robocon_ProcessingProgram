import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import processing.net.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ProcessingProgram extends PApplet {



Client myClient;
Serial serial,robot;
int cam = 123;
PImage img;
float Fx = 0,Fy = 0;
char put = 0;
target target1 = new target();
public void setup() {
  size(1280,720);
  frameRate(30);
  colorMode(RGB);
  myClient = new Client(this,"192.168.0.7",55555);
  serial = new Serial(this, "COM3", 9600);
  robot = new Serial(this,"COM8", 115200);
  img = loadImage("img_0.jpg");
}

public void draw() {
  if(serial.available()>0){
    cam =serial.read()-122;
  }
  if (myClient.available() > 0) {
    String dataIn = myClient.readStringUntil('\n');

    if ( dataIn != null ) {
      if ( dataIn.indexOf("Num=")==0 ) {
        int num = PApplet.parseInt(split(trim(dataIn),' ' )[1]);
        for (int i=0; i<num; i++) {
            String marker_info = myClient.readStringUntil('\n');
            if ( marker_info!= null ) {
              background(122);
              String[] data = split(trim(marker_info),',');
              String name = data[0];
              int px = PApplet.parseInt(data[1]);
              int py = PApplet.parseInt(data[2]);
              float x = PApplet.parseFloat(data[3]);
              float y = PApplet.parseFloat(data[4]);
              float z = PApplet.parseFloat(data[5]);
              Fx = sqrt(x*x+z*z)*sin((cam/123.0f)*PI/4.0f);
              Fy = sqrt(x*x+z*z)*cos((cam/123.0f)*PI/4.0f);
              image(img,Fx+width/2-50,Fy-500-50,100,100);
              fill(0, 102, 153);
              textSize(26);
              text("Tracking Object",Fx+width/2+100, Fy-500);
              serial.write((int)(px/640.0f*255));
            }
        }
      }
    }
    target1.x = 100;
    target1.y = 700;
    ellipse(100+width/2,700-500,100,100);
    target1.GetRelativePolarCordinates(Fx,Fy);
    strokeWeight(10);
    text(degrees(-target1.theta),100,100);
    line(Fx+width/2,Fy-500,(100*cos(-target1.theta+PI/2)+Fx+width/2),PApplet.parseInt(100*sin(-target1.theta+PI/2)+Fy-500));
    /*robot.write(255);*/
    robot.write(PApplet.parseInt(-target1.theta)/254);
    /*if (target1.r> 200){
      put = 200;
    }
    robot.write(put);*/
    myClient.clear();
  }
}
class target{
  float x,y,theta,r;
  target(){
    x = 0;
    y = 0;
    theta =0;
    r = 0;
  }
  public void GetRelativePolarCordinates(float robotx,float roboty){
    {
      theta = atan2(x-robotx,y-roboty);
      r = (x-robotx)*(x-robotx)+(y-roboty)*(y-roboty);
    }
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ProcessingProgram" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

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

//Imports


//Defines
public static final int NumOfTarget= 10;
Client myClient;
Serial serial,robot;
int MODE = 1; //1 : READ 0 : WRITE
int start = 0;
int cam = 123,targetcount = 0;
PImage img;
float Fx = 0,Fy = 0,x = 0,y=0,z=0;
char put = 0;
Table targets;
//target initialization
target[] target = new target[NumOfTarget];
//Program init
public void setup()
{
  for(int i = 0; i < NumOfTarget; i++)
  {
    target[i] = new target();
  }
  /*
  if(MODE == 1){
    targets = loadTable("data/target.csv");
    int i = 0;
    for(TableRow row : targets.rows()){
      target[i].x = row.getFloat("x");
      target[i].y = row.getFloat("y");
      i++;
    }
  }else{
    targets = new Table();
    targets.addColumn("x");
    targets.addColumn("y");
  }*/
  size(1280,720);
  frameRate(30);
  colorMode(RGB);
  myClient = new Client(this,"192.168.0.9",55555);
  serial = new Serial(this, "COM3", 9600);
  robot = new Serial(this,"COM8", 115200);
  img = loadImage("img_0.jpg");
  target[0].x = 134;
  target[0].y = 500;
  target[1].x = 125;
  target[1].y = 964;
  target[2].x = -60;
  target[2].y = 1000;
  target[3].x = -60;
  target[3].y = 1210;
  target[4].x = 150;
  target[4].y = 1220;
  target[5].x = 170;
  target[5].y = 1450;
  target[6].x = -80;
  target[6].y = 1425;
  target[7].x = -9;
  target[7].y = 1760;
  target[8].x = 170;
  target[8].y = 1670;
  target[9].x = 200;
  target[9].y = 2674;

}
//Main loop
public void draw()
{
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
              background(255);
              String[] data = split(trim(marker_info),',');
              String name = data[0];
              int px = PApplet.parseInt(data[1]);
              int py = PApplet.parseInt(data[2]);
              x = PApplet.parseFloat(data[3]);
              y = PApplet.parseFloat(data[4]);
              z = PApplet.parseFloat(data[5]);
              float camtheta = (cam/123.0f)*PI/2.0f;
              RelativeToAbs(camtheta);
              Fx = x;
              Fy = z;
              image(img,Fx+width/2-50,Fy-500-50,100,100);
              fill(0, 102, 153);
              textSize(26);
              text("Fx :"+Fx,100,50);
              text("Fy :"+Fy,100,100);
              text("Tracking Object",Fx+width/2+100, Fy-500);
              serial.write((int)(px/800.0f*255));
            }
        }
      }
    }
    ellipse(target[targetcount].x+width/2,target[targetcount].y-500,100,100);
    target[targetcount].GetRelativePolarCordinates(Fx,Fy);
    strokeWeight(10);
    //text(degrees(-target1.theta),100,100);
    line(Fx+width/2,Fy-500,(100*cos(-target[targetcount].theta+PI/2)+Fx+width/2),100*sin(-target[targetcount].theta+PI/2)+Fy-500);
    if(start%2==0){
      robot.write(255);
      text("STOP",100,300);
    }else if(targetcount == 9){
      robot.write(127);
    }else{
      robot.write(PApplet.parseInt((target[targetcount].theta+PI)/(2*PI)*254));
      text("MOVING",100,300);
    }
    text("theta :" +PApplet.parseInt((target[targetcount].theta+PI)/(2*PI)*254),100,150);
    text("r :"+target[targetcount].r,100,200);
    text("target :"+targetcount,100,250);
    /*if (target[targetcount].r> 200){
      put = 200;
    }*/
    //robot.write(put);
    if(target[targetcount].r < 100){
      if(targetcount<NumOfTarget-1){
        targetcount+= 1;
      }else{
        targetcount = 0;
      }
    }
    myClient.clear();
  }
}
class target
{
  float x,y,theta,r;
  target(){
    x = 0;
    y = 0;
    theta =0;
    r = 0;
  }
  public void GetRelativePolarCordinates(float robotx,float roboty)
  {
      theta = atan2(x-robotx,y-roboty);
      r = (x-robotx)*(x-robotx)+(y-roboty)*(y-roboty);
  }
}
public void RelativeToAbs(float theta2)
{
  float tempx,tempy;
  tempx = x;
  tempy = z;
  x = tempx*cos(theta2)-tempy*sin(theta2);
  z = tempx*sin(theta2)+tempy*cos(theta2);
}
public void mouseClicked(){
  if(targetcount<NumOfTarget-1){
    targetcount+= 1;
  }else{
    targetcount = 0;
  }
}
public void keyTyped(){
  if(key == PApplet.parseChar(ENTER)|| MODE == 0){
    TableRow newRow = targets.addRow();
    newRow.setFloat("x",Fx);
    newRow.setFloat("y",Fy);
    saveTable(targets,"data/target.csv");
  }else if(key == 'q'){
    exit();
  }else if (key ==' '){
    start++;
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

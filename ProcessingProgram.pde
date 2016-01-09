//Imports
import processing.serial.*;
import processing.net.*;
//Defines
public static final int NumOfTarget= 2;
Client myClient;
Serial serial,robot;
int cam = 123,targetcount = 0;
PImage img;
float Fx = 0,Fy = 0,x = 0,y=0,z=0;
char put = 0;
//target initialization
target[] target = new target[NumOfTarget];
//Program init
void setup()
{
  for(int i = 0; i < NumOfTarget; i++)
  {
    target[i] = new target();
  }
  size(1280,720);
  frameRate(30);
  colorMode(RGB);
  myClient = new Client(this,"192.168.0.5",55555);
  serial = new Serial(this, "COM3", 9600);
  robot = new Serial(this,"COM8", 115200);
  img = loadImage("img_0.jpg");
  target[0].x = 50;
  target[0].y = 1000;
  target[1].x = -50;
  target[1].y = 1000;
}
//Main loop
void draw()
{
  if(serial.available()>0){
    cam =serial.read()-122;
  }
  if (myClient.available() > 0) {
    String dataIn = myClient.readStringUntil('\n');

    if ( dataIn != null ) {
      if ( dataIn.indexOf("Num=")==0 ) {
        int num = int(split(trim(dataIn),' ' )[1]);
        for (int i=0; i<num; i++) {
            String marker_info = myClient.readStringUntil('\n');
            if ( marker_info!= null ) {
              background(255);
              String[] data = split(trim(marker_info),',');
              String name = data[0];
              int px = int(data[1]);
              int py = int(data[2]);
              x = float(data[3]);
              y = float(data[4]);
              z = float(data[5]);
              float camtheta = (cam/123.0)*PI/2.0;
              RelativeToAbs(camtheta);
              Fx = x;
              Fy = z;
              image(img,Fx+width/2-50,Fy-500-50,100,100);
              fill(0, 102, 153);
              textSize(26);
              text("Fx :"+Fx,100,50);
              text("Fy :"+Fy,100,100);
              text("Tracking Object",Fx+width/2+100, Fy-500);
              serial.write((int)(px/640.0*255));
            }
        }
      }
    }
    ellipse(target[targetcount].x+width/2,target[targetcount].y-500,100,100);
    target[targetcount].GetRelativePolarCordinates(Fx,Fy);
    strokeWeight(10);
    //text(degrees(-target1.theta),100,100);
    line(Fx+width/2,Fy-500,(100*cos(-target[targetcount].theta+PI/2)+Fx+width/2),100*sin(-target[targetcount].theta+PI/2)+Fy-500);
    robot.write(int((target[targetcount].theta+PI)/(2*PI)*254));
    text("theta :" +int((target[targetcount].theta+PI)/(2*PI)*254),100,150);
    text(targetcount,100,200);
    /*if (target[targetcount].r> 200){
      put = 200;
    }*/
    //robot.write(put);
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
  void GetRelativePolarCordinates(float robotx,float roboty)
  {
      theta = atan2(x-robotx,y-roboty);
      r = (x-robotx)*(x-robotx)+(y-roboty)*(y-roboty);
  }
}
void RelativeToAbs(float theta2)
{
  float tempx,tempy;
  tempx = x;
  tempy = z;
  x = tempx*cos(theta2)-tempy*sin(theta2);
  z = tempx*sin(theta2)+tempy*cos(theta2);
}
void mouseClicked(){
  if(targetcount<NumOfTarget-1){
    targetcount+= 1;
  }else{
    targetcount = 0;
  }
}

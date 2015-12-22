import processing.serial.*;
import processing.net.*; 
Client myClient;
Serial serial,robot;
int cam = 123;
PImage img;
float Fx = 0,Fy = 0;
char put = 0;
target target1 = new target(); 
void setup() {
  size(1280,720);
  frameRate(30);
  colorMode(RGB);
  myClient = new Client(this,"192.168.0.7",55555); 
  serial = new Serial(this, "COM3", 9600);
  robot = new Serial(this,"COM8", 115200);
  img = loadImage("img_0.jpg");
}
 
void draw() {
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
              background(122);   
              String[] data = split(trim(marker_info),',');
              String name = data[0];
              int px = int(data[1]);
              int py = int(data[2]);
              float x = float(data[3]);
              float y = float(data[4]);
              float z = float(data[5]);
              Fx = sqrt(x*x+z*z)*sin((cam/123.0)*PI/4.0);
              Fy = sqrt(x*x+z*z)*cos((cam/123.0)*PI/4.0);
              image(img,Fx+width/2-50,Fy-500-50,100,100);
              fill(0, 102, 153);
              textSize(26);
              text("Tracking Object",Fx+width/2+100, Fy-500);
              serial.write((int)(px/640.0*255));
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
    line(int(Fx+width/2),int(Fy-500),int(100*cos(-target1.theta+PI/2)+Fx+width/2),int(100*sin(-target1.theta+PI/2)+Fy-500));
    /*robot.write(255);*/
    robot.write(int(-target1.theta)/254);
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
  void GetRelativePolarCordinates(float robotx,float roboty){
    {
      theta = atan2(x-robotx,y-roboty);
      r = (x-robotx)*(x-robotx)+(y-roboty)*(y-roboty);
    }
  }
}

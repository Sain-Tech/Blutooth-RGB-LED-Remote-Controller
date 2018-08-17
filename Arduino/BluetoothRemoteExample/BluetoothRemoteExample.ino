#include <SoftwareSerial.h>

String cmd="";

SoftwareSerial mBluetoothSerial(2, 3);

int RED = 9;
int GREEN = 10;
int BLUE = 11;

int M = 4;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println("Send to Controller Device!");

  mBluetoothSerial.begin(9600);
  pinMode(M, OUTPUT);
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);
  
  digitalWrite(4, LOW);

  setColor(0, 0, 0);
}

void setColor(int r, int g, int b) {
  analogWrite(RED, r);
  analogWrite(GREEN, g);
  analogWrite(BLUE, b);
}

long rInt = 0;
long gInt = 0;
long bInt = 0;

void ColorConv(String mColorCode) {
  long number = strtol(&mColorCode[1], NULL, 16);
  
  rInt = number >> 16;
  gInt = number >> 8 & 0xFF;
  bInt = number & 0xFF;
}

void loop() {
  // put your main code here, to run repeatedly:

  while(mBluetoothSerial.available()) {
    char c_cmd = (char)mBluetoothSerial.read();
    cmd += c_cmd;
    delay(1);
  }

  if(cmd.charAt(1) != 'm' and cmd.charAt(0) == '<' and cmd.charAt(cmd.length()-1) == '>') {
       //첫 문자가 <, 끝 문자가 > 이면 
      int first = cmd.indexOf("/");
      int second = cmd.indexOf("/",first+1);
      //int strlength = str.length(); // 필요 없습니다.
      int third = cmd.indexOf(">",second); // 처음 '>'가 나타나는 위치 찾기
      String str1 = "#" + cmd.substring(1, first); // 첫 번째 토큰
      str1 += cmd.substring(first+1, second); //두 번째 토큰
      str1 += cmd.substring(second+1, third); //세 번째 토큰

      Serial.println("Color: "+str1);

      ColorConv(str1);
   }
  
  if (!cmd.equals("")) {
     Serial.println("input value: "+cmd);
    if(cmd == "<master_on>")
      digitalWrite(M, LOW);
    else if(cmd == "<master_off>")
      digitalWrite(M, HIGH);
    cmd = "";
    setColor(rInt, gInt, bInt);
  }

  //if (Serial.available()) {
    //mBluetoothSerial.write(Serial.read());
  //}
}

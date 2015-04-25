sdr-java
==================
Simple Software Defined Radio receiver for ADS-B and AIS in Java and JavaScript, using the Google maps API.
This project depends on:
* 'dump1090' by Salvatore Sanfilippo: https://github.com/antirez/dump1090
* 'aismessages' by Thomas Borg Salling: https://github.com/tbsalling/aismessages
 
Background
---
Using a compatible RTL-SDR usb stick (cost < $10) and this project, you'll be able to receive and decode signals 
from ADS-B transponders installed in commercial airliners and from AIS transponders installed in ships, and plot
flight/ship paths on Google maps.
 and plot flight paths on Google maps.

For a more in-depth introduction, see the following presentation:
* http://www.slideshare.net/BertJanSchrijver/jfall-2014-decoding-the-airspace-above-you-with-java-and-7-hardware

Dependencies
---
- Java 8, maven 3
- MongoDB
- For ADS-B: Running dump1090 client on the network. Preferably: tailor-made ADS-B antenna connected to the RTL-SDR device (see https://github.com/antirez/dump1090#antenna)
- For AIS: Running aisdecoder on the network. Preferably: tailor-made AIS antenna
- Compatible RTL-SDR device


RTL-SDR devices
---
I have used these two:
* http://www.aliexpress.com/item/RTL-SDR-FM-DAB-DVB-T-USB-2-0-Mini-Digital-TV-Stick-TV-Receiver-Portable/1670781135.html
* http://www.aliexpress.com/item/RTL-SDR-FM-DAB-DVB-T-USB-2-0-Mini-Digital-TV-Stick-DVBT-Dongle-SDR/1316276597.html

In general, when finding an RTL-SDR compatible device, just look for the best selling Realtek / RTL-SDR usb DVB-T stick.


Database setup
---
Install and start MongoDB. Database 'sdr' and collection 'positionData' will be created automatically.
Create the following index to speed up queries over lots of data:
```
db.positionData.ensureIndex({"timestamp": 1}, {"background": true});
```

Maven module structure
---
* connectors: contains input sources for AIS and ADS-B that write to MongoDB
* frontend: web frontend and API to display data written by the connectors
* services: shared services used by multiple modules (configuration and MongoDB services)


Setting up a Raspberry Pi as a remote ADS-B / AIS receiver
---

Update the system
```
sudo su -
apt-get update && apt-get upgrade
apt-get install raspi-copies-and-fills
```

Install RTL-SDR
```
apt-get install git cmake libusb-1.0-0-dev build-essential
git clone git://git.osmocom.org/rtl-sdr.git
cd rtl-sdr/
mkdir build && cd build
cmake ../
make
make install
ldconfig
```

Blacklist the stock libusb driver so rtl-sdr can do its magic
```
vi /etc/modprobe.d/raspi-blacklist.conf
```

Add the following lines:
```
blacklist dvb_usb_rtl28xxu
blacklist rtl2832
blacklist rtl2830
```

Reboot to activate the blacklisting
```
reboot
```

Install aisdecoder
```
apt-get install libasound2-dev libpulse-dev
wget http://www.aishub.net/downloads/aisdecoder-1.0.0.tar.gz
tar xfvz aisdecoder-1.0.0.tar.gz
cd aisdecoder-1.0.0
mkdir build && cd build
cmake ../ -DCMAKE_BUILD_TYPE=RELEASE
make
cp aisdecoder /usr/local/bin/
cp -a lib* /usr/local/lib/
ldconfig
```

Try out aisdecoder
```
rtl_fm -g 9999 -f 161975000 -p 10 -s 48k -r 48k | aisdecoder -h <ip-of-receiving-end> -p 1234 -a file -c mono -d -f /dev/stdin
```

Fiddle with value of '-p' to enable/maximize reception


Install dump1090
```
git clone git://github.com/MalcolmRobb/dump1090.git
cd dump1090
make
```

Start dump1090
```
./dump1090 --interactive --net --aggressive --metric --interactive-ttl 5
```




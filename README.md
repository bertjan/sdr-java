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
* http://www.slideshare.net/BertJanSchrijver/devoxx-uk-2015-decoding-the-air-around-you-with-java-and-7-hardware (slide deck)
* https://www.parleys.com/tutorial/decoding-air-around-you-java-7-hardware (recording)


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
db.positionData.ensureIndex({"timestamp": 1, "_id": 1, "heading" : 1, "latitude" : 1, "objectId" : 1, "objectType" : 1, "longitude" : 1}, {"background": true});
```
Create the following index to enable automatic cleanup of data older than a 7 days:
```
db.positionData.createIndex({ "createdAt": 1 }, { expireAfterSeconds: 604800 });
```

Maven module structure
---
* connectors: contains input sources for AIS and ADS-B that write to MongoDB
* frontend: web frontend and API to display data written by the connectors
* services: shared services used by multiple modules (configuration and MongoDB services)


Setting up a Raspberry Pi as a remote ADS-B / AIS receiver
---
Prerequisite: install the latest Raspbian Linux release on the Pi. 
Instructions below have been tested on a Raspberry Pi model B+.

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


Bonus 1: decoding POCSAG pager messages
---

Install multimon-ng:

```
apt-get install qt4-qmake
git clone https://github.com/EliasOenal/multimon-ng.git
cd multimon-ng

# Skip X11 support, configure raw input only
cat multimon-ng.pro | sed 's@-lX11@@g' > multimon-ng.tmp && mv multimon-ng.tmp multimon-ng.pro
echo 'DEFINES += NO_X11' >> multimon-ng.pro 
echo 'DEFINES += ONLY_RAW' >> multimon-ng.pro 

mkdir build && cd build
qmake ../multimon-ng.pro
make && make install
```

Fire up rtl_fm and pipe the output throuh multimon:
```
rtl_fm -f 172446000 -s 22050 -M fm -F 0 -E dc -g 100 | multimon-ng -q -t raw -a POCSAG1200 -a POCSAG512 -a POCSAG2400 -e -p -v1 /dev/stdin
```

Bonus 2: decoding FLEX pager messages (used for P2000 in the Netherlands)
---
Install gnuradio and gr-osmosdr. 

```
echo 'deb http://archive.raspbian.org/raspbian jessie main' >> /etc/apt/sources.list
apt-get update && apt-get upgrade
apt-get install gnuradio gnuradio-dev libboost-all-dev gr-osmosdr
```

Apply a patch to fix an issue in volk (part of gnuradio 3.7.5),
as described on https://batilanblog.wordpress.com/2015/02/17/using-ec3k-with-raspberry-pi/:

```
wget http://gnuradio.org/releases/gnuradio/gnuradio-3.7.5.tar.gz
tar xvzf gnuradio-3.7.5.tar.gz 
cd gnuradio-3.7.5/
wget http://gnuradio.org/redmine/attachments/download/821/0001-volk-Fix-volk_malloc-when-alignment-is-1.patch
patch -p1 < *volk*.patch
mkdir build && cd build
cmake -DENABLE_DEFAULT=Off -DENABLE_VOLK=True -Dhave_mfpu_neon=0 ..
make
cp volk/lib/libvolk.so.0.0.0 /usr/lib/arm-linux-gnueabihf/
ldconfig
```

Install and run the flex decoder:
```
git clone https://github.com/zarya/sdr
cd sdr/receivers/flex/
chmod +x rtl_flex_noX.py 
./rtl_flex_noX.py -f 169.645M --rx-gain=37.2 --device=rtl=0
```
#!/bin/sh

PRESERVECONFIG=0
if [ -f /opt/telescope/conf/telescope.xml ]
then
    cp /opt/telescope/conf/telescope.xml /opt/telescope/conf/telescope.xml.saved
    PRESERVECONFIG=1
fi

mkdir -p /opt/telescope
cp -r * /opt/telescope
chmod -R go+rX /opt/telescope

if [ ${PRESERVECONFIG} -eq 1 ] && [ -f /opt/telescope/conf/telescope.xml.saved ]
then
    mv -f /opt/telescope/conf/telescope.xml.saved /opt/telescope/conf/telescope.xml
fi

mv /opt/telescope/telescope.service /etc/systemd/system
chmod 664 /etc/systemd/system/telescope.service

systemctl daemon-reload
systemctl enable telescope.service

rm /opt/telescope/setup.sh
rm -r ../out

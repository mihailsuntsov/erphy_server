# DokioCRM<br>
This is a server part of DokioCRM
## About
DokioCRM is a free and open source Ecommerce ERP/CRM for small business. You can manage your customers, sales and finances, and get analytics of how your business works.<br>
It has an integration with WordPress and WooCommerce, and you can put all your goods from CRM to the online store in one click!<br>
There is a support of a multilingual goods and online stores<br>

## Site
https://dokio.me<br>

## Examples
The SaaS version: https://erphy.me/dss<br>
The simple example with product filters: https://dokio.me/shop/<br>
Multilingual real estate catalog: https://dokio.me/examples/realestate_en/<br>
Multilingual food delivery site: http

## Contacts
Mikhail Suntsov<br>
E-mail: mihail.suntsov@gmail.com<br>
Telegram: @eager_beager<br>
LinkedIn: Mikhail Suntsov<br>

## How to install
You can install the program on your VPS server using a bash script. 
Bash script tested on Ubuntu 22.04<br>
Minimum requirments of VPS is: 1 Cpu core, 2 Gb RAM, 15 Gb HDD<br>
Use only clear VPS for the installation<br>
In order for SSL to install and work correctly, I recommend set the domain name for your VPS.
1. Login on your VPS as root user
2. Check the system hostname:
```shell
hostname -f
```
The output shoult show the correct hostname, for example, `yoursite.com`<br>
If the hostname is incorrect - set the correct hostname:
```shell
hostnamectl set-hostname yoursite.com
```
Then check it again:
```shell
hostname -f
```
NOTE: _In order for the SSL certificate to be obtained successfully, the server must be accessible by domain name. If you have just received a domain name, the VPS server may not be available yet. You can check whether or not the domain is resolving into the correct host IP address by using the ping command in your computer console:`ping yoursite.com`_
3. Execute:
```shell
wget https://dokio.me/downloads/shell/dokiocrm-install.sh  -O - | sh
```
This command will start the installation process. 

Once DokioCRM is installed, the installer will ask you if you want to install a WordPress site with a built-in WooCommerce store. If you want to do this, type `y` and press Enter. 
A configured and ready-to-use online store will be installed.


After the installation the file `/var/dokiocrm.key` will be created. It contans all passwords. I recommend saving this information on your computer and deleting this file.<br><br>
The user interface of DokioCRM will be here: `https://yoursite.com/dss`.<br>
Click `Registration`, and register. After registration you can login into the user interface of DokioCRM.<br>
When the first account is registered, the ability to register new users will not be available. This is to prevent new unwanted registrations on your server. However, you can create any number of users of your company from the user interface (Settings/Users)

NOTE: _Some mail services, for example, Gmail, can bounce the emails from your server mailbox. It's because the domain zone of yourserver.com has no SPF record. Add a TXT record for `yoursite.com`. It should be like `"v=spf1 +a +mx +ip4:xxx.xxx.xxx.xxx ~all"`, created in a domain zone management on the domain `yoursite.com` registrar website. <br>
For some services it can not be enough, and you should set up DKIM, DMARC on your server, and order the creation of a PTR record from your domain name registrar._


## How to use
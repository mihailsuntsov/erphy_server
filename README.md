# ERPHY<br>
This is a server part of ERPHY (ex DokioCRM)
## About
ERPHY is a free and open source Ecommerce ERP/CRM. You can manage your customers, warehouse, sales and finances, and get analytics of how your business works.<br><br>
NEW!!!<br>
Added an universal reservation and appointment module. Now ERPHY is a powerful reservation and appointment system!
Suitable for beauty salons, hostels, hotels, hospitals, dentists, car rental and other!<br><br>
It has an integration with WordPress and WooCommerce, and you can put all your goods from CRM to the online store in one click!<br>
There is a support of a multilingual goods and online stores<br>

## Interface examples

[<img src="https://erphy.me/downloads/pictures/interface/main_page.jpg" width="49%">](https://erphy.me/downloads/pictures/interface/main_page.png "Full size")
[<img src="https://erphy.me/downloads/pictures/interface/calendar1.jpg" width="49%">](https://erphy.me/downloads/pictures/interface/calendar1.png "Full size")
[<img src="https://erphy.me/downloads/pictures/interface/calendar2.jpg" width="49%">](https://erphy.me/downloads/pictures/interface/calendar2.png "Full size")
[<img src="https://erphy.me/downloads/pictures/interface/calendar3.jpg" width="49%">](https://erphy.me/downloads/pictures/interface/calendar3.png "Full size")
[<img src="https://erphy.me/downloads/pictures/interface/appointment1.jpg" width="49%">](https://erphy.me/downloads/pictures/interface/appointment1.png "Full size")
[<img src="https://erphy.me/downloads/pictures/interface/scedule.jpg" width="49%">](https://erphy.me/downloads/pictures/interface/scedule.png "Full size")
<br>

## Site
https://erphy.me/crm/en<br>

## Examples
The SaaS version: https://erphy.me/dss Here you can register and test working with the program interface<br>
Multilingual food delivery site: https://food.erphy.me

## How to install
You can install the program on your VPS server using a bash installation script. 
Bash installation script tested on:<br> 

* Ubuntu Server 22.04 <br>
* Ubuntu Server 24.04 <br>

Minimum requirments of VPS is: 1 Cpu core, 2 Gb RAM, 15 Gb HDD<br>
Use only freshly installed VPS for the installation of ERPHY<br>

In order for SSL to install and work correctly, you should order and configure a domain name for your VPS.

NOTE: _yoursite.com - the example name of your domain address. Replace it with your real address._
### 1. Login to your VPS
Use PuTTY or any another SSH client to access to your VPS as a root user
### 2. Check the system hostname:
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
### 3. Execute installation script:
```shell
wget https://erphy.me/downloads/shell/erphy-install.sh  -O - | sh
```
This command will start the installation process. 

Once ERPHY is installed, the installer will ask you if you want to install a WordPress site with a built-in WooCommerce store. If you want to do this, type `y` and press Enter. 
A configured and ready-to-use online store will be installed.

## How to use
After the installation the file `/var/erphy_pwd.txt` will be created. It contans all passwords. I recommend saving this information on your computer and deleting this file.<br><br>
The user interface of ERPHY will be available at this address: `https://yoursite.com/dss`.<br>
Click `Registration`, and create your account. After registration you can login into the user interface of ERPHY.<br>
When the first account is registered, the ability to register new accounts will not be available. This is to prevent new unwanted account registrations on your server. However, you can create any number of users of your company from the user interface (Settings/Users). But if you want to allow new registrations on your server, run the following command in the SSH client:
```shell                                                                                                                                                                                                                                                                 
sudo -Hiu postgres psql -d erphy -c "update settings_general set show_registration_link = true, allow_registration = true;"
```
NOTE: _Some mail services, for example, Gmail, can bounce the emails from your server mailbox. It's because the domain zone of `yoursite.com` has no SPF record. Add a TXT record for `yoursite.com`. It should be like `"v=spf1 +a +mx +ip4:xxx.xxx.xxx.xxx ~all"`, created in a domain zone management on the domain `yoursite.com` registrar website. <br>
For some services it still can not be enough, and you should set up DKIM and DMARC on your server, and order the creation of a PTR record from your VPS provider company._

Detailed instructions for use - in [Knowledge base](https://erphy.me/crm/en/knowledge-base/)


## Contacts
Mikhail Suntsov<br>
E-mail: mihail.suntsov@gmail.com<br>
Telegram: @eager_beager<br>
LinkedIn: Mikhail Suntsov<br>
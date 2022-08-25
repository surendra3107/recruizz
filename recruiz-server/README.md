Steps to install and configure recruiz UI in local for development:

1.Install Git for windows. It can be downloaded from https://git-scm.com/download/win. While installing choose "Use Git from the windows Command Prompt". 

Check git installation by running following command

```
git --version
```
2.Install node.js. It can be downloaded from https://nodejs.org/en/download/. Check node.js installation by running
```
node --version
```
Clone the project to your local machine

3.Install application requirements

Open windows command prompt and navigate to the project folder. 

Run the following commands one by one
```
npm install -g grunt-cli bower
```
```
npm install
```
```
bower install
```
4. Run the application

If everything installed properly, run the following command. This will run the build process and open the application in browser. Port 82 will be used, so make sure it is available in local machine.

```
grunt
```

To deploy application to production, run the following command and deploy files from builds/dest to production server.

```
grunt prod
```
Note: Keep the command prompt open. The grunt will watch the project folder for any file change. If there is a change, grunt will process it automatically and the change will reflect in the browser. (No need to refresh browser for every change)

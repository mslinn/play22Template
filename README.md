# Micronautics Online Course Creator (MOCC) #

This web application provides online content authoring and secure, scalable delivery from S3.
It runs under Play Framework v2.2.0 with Scala.

This project is owned and managed by [Micronautics Research Corporation](http://www.micronauticsresearch.com/)

## To Build ##

This project uses the [AwsS3](https://github.com/mslinn/AwsS3/) library.
`AwsS3` requires Java 7, therefore `mocc` also requires Play Framework to run under Java 7.

For all OSes, you need to define a Java system property before you can run this web app locally.
The system property `com.amazonaws.sdk.disableCertChecking` must be set `true`.
For local execution, the `bin/play` script sets this property.
For Heroku, the `system.properties` file sets this value.

    export JAVA_OPTS="$JAVA_OPTS -Dcom.amazonaws.sdk.disableCertChecking=true"

### Environment variables ###
The `PUB_CONFIG` environment variable determines the publisher configuration to display. Values are:
  1. ScalaCourses
  2. IndieCourses
  3. MLCourses

Suggested settings for the environment variables are:

````
export SMTP_HOST="secure.emailsrvr.com"
export SMTP_PORT=465
export SMTP_SSL=true
export SMTP_TLS=true
export SMTP_USER="mslinn@micronauticsresearch.com"
export SMTP_PASSWORD="secret"
export SMTP_FROM="ScalaCourses Mailbot <support@micronauticsresearch.com>"
export SUPERUSER_USERID=root
export SUPERUSER_EMAIL="admin@mslinn.com"
export SUPERUSER_FIRST_NAME=Mike
export SUPERUSER_LAST_NAME=Slinn
export SUPERUSER_PASSWORD=secret
export JAVA_OPTS="-Dcom.amazonaws.sdk.disableCertChecking=true"
export DATABASE_MAX_CONNS_PER_PARTITION=75
export DATABASE_MIN_CONNS_PER_PARTITION=10
export PUB_CONFIG=1
````

This environment variable is required by the `bin/play` script:

    export PLAY21_HOME=/opt/play-2.2.0

## Running on Heroku ##
A custom `Procfile` is provided in the application root that defines the Java system variable
`com.amazonaws.sdk.disableCertChecking`, which configures the HttpClient SSL factory so the AWS SSL certificate is accepted.

    heroku create scalacourses # You will need to use another name for your Heroku app instance
    git clone git@bitbucket.org:mslinn/mocc.git
    git remote add heroku git@heroku.com:scalacourses.git # use your Heroku app name here

To debug on Heroku, [read this](http://mikeslinn.blogspot.com/2012/09/debugging-jvm-programs-on-heroku.html).
The `enableHerokuDebugging` and `disableHerokuDebugging` scripts are helpful.

Debug the app:

    heroku logs --tail

Debug the database:

    heroku logs --tail --ps postgres --app data-services-24bb7ab0

### Not True Any More ###
If the regular buildpack is used, the build will run out of PermGen space.
If we did not want to use a custom buildpack, we could enable
[user-env-compile](https://devcenter.heroku.com/articles/labs-user-env-compile#enabling).
However, it is better to use a [custom buildpack](https://github.com/heroku/heroku-buildpack-scala.git):

    heroku config:add BUILDPACK_URL=https://github.com/heroku/heroku-buildpack-scala.git#perm-gen

### Environment variables ###
All of the Heroku command lines require the `--app` parameter, which is implied for all of the remaining example Heroku commands in this document.
Values are `scalacourses`, `trainingadmin`, `trainingadmin2`, `indiecourses` and `mlcourses`.
For example:

    heroku config --app scalacourses
    heroku config --app trainingadmin
    heroku config --app trainingadmin2
    heroku config --app indiecourses
    heroku config --app mlcourses

### Heroku Runtime Settings ###
[Heroku 1x Dynos](https://devcenter.heroku.com/articles/dynos) have 512MB RAM; 2x dynos have 1GB RAM.
The following `JAVA_OPTS` settings work on all dynos without error, and use all available RAM.

    HEROKU_JAVA_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:MaxPermSize=384M"
    HEROKU_JAVA_OPTS="$HEROKU_JAVA_OPTS -Dlogger.file=conf/logback-prod.xml -XX:+CMSClassUnloadingEnabled"
    heroku config:add JAVA_OPTS="$HEROKU_JAVA_OPTS"
    heroku config:add BRANDING_COPY_FILES=true
    heroku config:add BRANDING_THEME=redmond
    heroku config:add SMTP_FROM="Fred Flintstone <noreply@rubble.com>"
    heroku config:add SMTP_HOST=asdf.asfd.com SMTP_PORT=465 SMTP_SSL=Yes SMTP_TLS=Yes
    heroku config:add SMTP_USER="asdf@asdf.com" SMTP_PASSWORD=asfdasdf
    heroku config:add PAYPAL_MODE="live"
    heroku config:add PAYPAL_RECEIVER_EMAIL="asdf@asdf.com"
    heroku config:add cdn.importMedia="file:///work/training/private_media" #TODO figure out the directory
    heroku config:add cdn.importCode="file:///work/training/private_code"   #TODO figure out the directory
    heroku config:add cdn.exportMedia="file:///work/training/export"        #TODO figure out the directory
    heroku config:add cdn.exportCode="file:///work/training/export"         #TODO figure out the directory
    heroku config:add CDN_MINUTES_VALID=60
    heroku config:add exportMediaUrl="http://www.scalacoursestest.com/private_media"
    heroku config:add exportCodeUrl="https://bitbucket.org/mslinn/udemy_scalajavainterop_oocompat_code"

    heroku config:add SUPERUSER_USERID=root
    heroku config:add SUPERUSER_EMAIL="mslinn@micronauticsresearch.com"
    heroku config:add SUPERUSER_FIRST_NAME=Mike
    heroku config:add SUPERUSER_LAST_NAME=Slinn
    heroku config:add SUPERUSER_PASSWORD=notTelling
    heroku config:add ENABLE_BETA=false

    heroku config:add SHOW_ALL=false
    heroku config:add PUB_CONFIG=1

    heroku config:set DATABASE_MAX_CONNS_PER_PARTITION=75
    heroku config:set DATABASE_MIN_CONNS_PER_PARTITION=10
    heroku config:set DATABASE_URL:postgres://uff6en11uopb74:p4teaf69admpfn83v9ougubds1c@ec2-54-225-193-251.compute-1.amazonaws.com:5442/dbl2g51aq7lpnu?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory

Check that the environment variables are properly set:

    $ heroku config
    === trainingadmin Config Vars
    BUILDPACK_URL: https://github.com/jamesward/heroku-buildpack-scala
    SUPERUSER_USERID: root
    SMTP_FROM: asdfasdf
    SMTP_HOST: asdf.asfd.com
    SMTP_PORT: 465
    SMTP_SSL: Yes
    SMTP_TLS: Yes
    SMTP_USER: "asdf@asdf.com"
    SMTP_PASSWORD: asdfasdf
    PAYPAL_MODE: "live"
    PAYPAL_RECEIVER_EMAIL: "asfd@asdf.com"
    cdn.importMedia="file:///work/training/private_media"
    cdn.importCode="file:///work/training/private_code"
    cdn.exportMedia="file:///work/training/export"
    cdn.exportCode="file:///work/training/export"
    cdn.exportMedia="file:///work/training/export"
    cdn.exportCode="file:///work/training/export"
    CDN_MINUTES_VALID=60
    exportMediaUrl="http://www.scalacoursestest.com/private_media"
    exportCodeUrl="https://bitbucket.org/mslinn/udemy_scalajavainterop_oocompat_code"
    SUPERUSER_USERID=root
    SUPERUSER_EMAIL="mslinn@micronauticsresearch.com"
    SUPERUSER_FIRST_NAME=Mike
    SUPERUSER_LAST_NAME=Slinn
    SUPERUSER_PASSWORD=notTelling
    SHOW_ALL=false
    PUB_CONFIG=1
    DATABASE_MAX_CONNS_PER_PARTITION=75
    DATABASE_MIN_CONNS_PER_PARTITION=10

### Deploying to Heroku ###
Set the Heroku buildpack for Play 2.2.0 with this:

    heroku config:add BUILDPACK_URL=https://github.com/jamesward/heroku-buildpack-scala

Deploy the `master` branch to http://scalacourses.com like this:

    git push scalacourses master

Deploy the `play2.2.0-1` branch to http://mlcoursesonline.com like this:

    git push mlcourses play2.2.0-1:master

### Heroku DNS ###

    heroku domains:add www.mlcoursesonline.com

#### Namecheap ####

    @ http://www.mlcoursesonline.com/  URLRedirect 60
    www mlcourses.herokuapp.com. CNAME 60

### Postgres ###

Postgres documentation is [here](https://devcenter.heroku.com/articles/heroku-postgresql).
Install it like this:

    $ heroku addons:add heroku-postgresql:basic --app trainingadmin
    Adding heroku-postgresql:basic on trainingadmin... done, v35 ($9/mo)
    Attached as HEROKU_POSTGRESQL_BROWN_URL
    Database has been created and is available
     ! This database is empty. If upgrading, you can transfer data from another database with pgbackups:restore.

Use the `bin/backup` command provided with this project to backup the Heroku database.
You normally want to `git commit` the database after backing it up.

    $ bin/backup heroku

`bin/backup` can also backup the local database.

    $ bin/backup

Use the `bin/restore` command provided with this project to backup the Heroku database:

    $ bin/restore heroku

The `bin/restore` command can also restore the local database:

    $ bin/restore

## Unit Tests ##
The `bin/test` script runs Spec2 and/or ScalaTest unit tests. Use it like this:

    bin/test controllers.UploadTest

For IntelliJ IDEA and Eclipse (if you dare use it), put this in test run configurations:

    -Dconfig.file=conf/test.conf

## Google OATH Configuration ##

This has been put on hold.

`http://www.scalacourses.com` is a web app with redirect URI `http://www.scalacourses.com/oauth2callback`
as defined by `https://code.google.com/apis/console/?api=calendar&pli=1#project:552677350300:access`
and stored in `public/scalacourses_client_secrets.json`

````
Client ID:
552677350300.apps.googleusercontent.com
Email address:
552677350300@developer.gserviceaccount.com
Client secret:
sASKknswt369IfS2PTMpw7-w
Redirect URIs:	http://www.scalacourses.com/oauth2callback
JavaScript origins:	http://www.scalacourses.com
````

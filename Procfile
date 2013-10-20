#web: target/start -Dcom.amazonaws.sdk.disableCertChecking=true -Dhttp.port=${PORT} ${JAVA_OPTS}
# -Xmx384m -Xss512k -XX:+UseCompressedOops
web: target/universal/stage/bin/mocc -Dhttp.port=${PORT} -Dcom.amazonaws.sdk.disableCertChecking=true -Dlogger.file=conf/logback-prod.xml

import java.util.concurrent.TimeUnit
long lastRestarted = Jenkins.instance.toComputer().getConnectTime()
long now =  System.currentTimeMillis()
println TimeUnit.MILLISECONDS.toDays(now - lastRestarted)

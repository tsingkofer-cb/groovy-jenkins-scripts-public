import com.cloudbees.jenkins.plugins.assurance.NonCapPluginsMonitor
import jenkins.model.Jenkins

def monitor = Jenkins.get().getExtensionList(NonCapPluginsMonitor.class).first()

if (monitor != null) {

    Jenkins.get().pluginManager.plugins.each { plugin ->
        try {
            monitor.doAcknowledge(plugin.shortName)
        } catch (Exception e) {
            // Ignore plugins that are not flagged by the monitor
        }
    }

    println "Completed acknowledgement pass."
} else {
    println "NonCapPluginsMonitor extension not found."
}

package net.carnagepvp.vaultsremake.core.config.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigField(val path: String = "", val comment: Array<String> = [])

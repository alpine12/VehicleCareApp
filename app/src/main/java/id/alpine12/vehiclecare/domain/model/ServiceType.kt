package id.alpine12.vehiclecare.domain.model

enum class ServiceType(val displayName: String) {
    GANTI_OLI("Ganti Oli"),
    SERVIS("Servis Motor"),
    TAMBAH_ANGIN("Tambah Angin"),
    GANTI_AIR_RADIATOR("Ganti Air Radiator"),
    LAINNYA("Lainnya")
}

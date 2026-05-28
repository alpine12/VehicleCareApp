package id.alpine12.vehiclecare.domain.model

enum class ServiceType(val displayName: String) {
    GANTI_OLI("Ganti Oli"),
    SERVIS_ROUTIN("Servis Rutin"),
    TAMBAH_ANGIN("Tambah Angin"),
    GANTI_PART("Ganti Part"),
    PAJAK("Pajak"),
    AKI("Aki"),
    BAN("Ban"),
    LAINNYA("Lainnya")
}

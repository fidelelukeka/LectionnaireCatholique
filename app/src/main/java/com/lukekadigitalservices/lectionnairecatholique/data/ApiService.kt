package com.lukekadigitalservices.lectionnairecatholique.data

import com.google.gson.annotations.SerializedName

// --- Partie Commune ---
data class LiturgieInfo(
    val date: String,
    val zone: String,
    val couleur: String,
    val annee: String?,
    @SerializedName("temps_liturgique") val tempsLiturgique: String?,
    val semaine: String?,
    val jour: String?,
    @SerializedName("jour_liturgique_nom") val jourLiturgiqueNom: String?,
    val fete: String?,
    val degre: String?,
    val ligne1: String?,
    val ligne2: String?,
    val ligne3: String?,
    val couleur2: String?,
    val couleur3: String?
)

// --- Endpoint /informations ---
data class InfoResponse(
    val informations: LiturgieInfo
)

// --- Endpoint /messes ---
data class MessesResponse(
    val informations: LiturgieInfo,
    val messes: List<Messe>
)

data class Messe(
    val nom: String,
    val lectures: List<MesseLecture>
)

data class MesseLecture(
    val type: String,
    @SerializedName("refrain_psalmique") val refrainPsalmique: String?,
    @SerializedName("ref_refrain") val refRefrain: String?,
    val titre: String?,
    val contenu: String, // HTML
    val ref: String?,
    @SerializedName("intro_lue") val introLue: String?,
    @SerializedName("verset_evangile") val versetEvangile: String?
)

// --- Endpoint /lectures (Office des lectures) ---
data class OfficeResponse(
    val informations: LiturgieInfo,
    val lectures: OfficeContent
)

data class OfficeContent(
    val introduction: String?,
    val hymne: Hymne?,
    @SerializedName("psaume_1") val psaume1: Psaume?,
    val lecture: LectureDetail?,
    @SerializedName("titre_patristique") val titrePatristique: String?,
    @SerializedName("texte_patristique") val textePatristique: String?,
    val oraison: String?
)

data class Hymne(val titre: String?, val texte: String?)
data class Psaume(val reference: String?, val texte: String?)
data class LectureDetail(val reference: String?, val titre: String?, val texte: String?)
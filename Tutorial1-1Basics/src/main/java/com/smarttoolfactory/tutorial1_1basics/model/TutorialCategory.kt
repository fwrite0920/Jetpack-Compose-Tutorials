package com.smarttoolfactory.tutorial1_1basics.model

/** One registration drives the home pager, search index and navigation destinations. */
internal data class TutorialCategory(
    val title: String,
    val tutorials: List<TutorialSectionModel>
)

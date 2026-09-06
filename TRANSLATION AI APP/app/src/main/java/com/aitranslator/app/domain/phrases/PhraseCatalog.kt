package com.aitranslator.app.domain.phrases

/**
 * Static, hand-authored phrase content — no external source, no network
 * call needed to populate it. Kept as a plain Kotlin object rather than a
 * database table since this list is fixed at build time, not something
 * the user edits or that changes at runtime.
 */
object PhraseCatalog {

    val phrases: List<Phrase> = listOf(
        // Greetings
        Phrase("greet_1", PhraseCategory.GREETINGS, "Hello, how are you?"),
        Phrase("greet_2", PhraseCategory.GREETINGS, "Good morning"),
        Phrase("greet_3", PhraseCategory.GREETINGS, "Good evening"),
        Phrase("greet_4", PhraseCategory.GREETINGS, "Nice to meet you"),
        Phrase("greet_5", PhraseCategory.GREETINGS, "Goodbye, see you later"),
        Phrase("greet_6", PhraseCategory.GREETINGS, "Thank you very much"),
        Phrase("greet_7", PhraseCategory.GREETINGS, "You're welcome"),
        Phrase("greet_8", PhraseCategory.GREETINGS, "Excuse me"),

        // Restaurant
        Phrase("rest_1", PhraseCategory.RESTAURANT, "A table for two, please"),
        Phrase("rest_2", PhraseCategory.RESTAURANT, "Can I see the menu, please?"),
        Phrase("rest_3", PhraseCategory.RESTAURANT, "I would like to order"),
        Phrase("rest_4", PhraseCategory.RESTAURANT, "The bill, please"),
        Phrase("rest_5", PhraseCategory.RESTAURANT, "Is this dish spicy?"),
        Phrase("rest_6", PhraseCategory.RESTAURANT, "I am allergic to nuts"),
        Phrase("rest_7", PhraseCategory.RESTAURANT, "Water, please"),
        Phrase("rest_8", PhraseCategory.RESTAURANT, "It was delicious"),

        // Travel
        Phrase("trav_1", PhraseCategory.TRAVEL, "Where is the train station?"),
        Phrase("trav_2", PhraseCategory.TRAVEL, "How much is a ticket to the airport?"),
        Phrase("trav_3", PhraseCategory.TRAVEL, "Is this the right platform?"),
        Phrase("trav_4", PhraseCategory.TRAVEL, "Can you call me a taxi?"),
        Phrase("trav_5", PhraseCategory.TRAVEL, "I am lost"),
        Phrase("trav_6", PhraseCategory.TRAVEL, "How far is it from here?"),
        Phrase("trav_7", PhraseCategory.TRAVEL, "Where can I find a hotel?"),

        // Shopping
        Phrase("shop_1", PhraseCategory.SHOPPING, "How much does this cost?"),
        Phrase("shop_2", PhraseCategory.SHOPPING, "Do you accept credit cards?"),
        Phrase("shop_3", PhraseCategory.SHOPPING, "Can I try this on?"),
        Phrase("shop_4", PhraseCategory.SHOPPING, "Do you have a smaller size?"),
        Phrase("shop_5", PhraseCategory.SHOPPING, "I am just looking, thank you"),
        Phrase("shop_6", PhraseCategory.SHOPPING, "Can I get a discount?"),

        // Emergency
        Phrase("emer_1", PhraseCategory.EMERGENCY, "I need help"),
        Phrase("emer_2", PhraseCategory.EMERGENCY, "Call an ambulance, please"),
        Phrase("emer_3", PhraseCategory.EMERGENCY, "Where is the nearest hospital?"),
        Phrase("emer_4", PhraseCategory.EMERGENCY, "I lost my passport"),
        Phrase("emer_5", PhraseCategory.EMERGENCY, "Please call the police"),
        Phrase("emer_6", PhraseCategory.EMERGENCY, "I don't feel well")
    )

    fun byCategory(category: PhraseCategory): List<Phrase> {
        return phrases.filter { it.category == category }
    }
}
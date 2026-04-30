package com.api.tinyfarm.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomNameProvider {
    private static final List<String> MALE_NAMES = List.of(
        "Zeus", "Ares", "Hermes", "Apollo", "Poseidon", "Hades", "Hephaistos", "Dionysos", "Perseus", "Achille",
        "Odysseus", "Theseus", "Jason", "Orion", "Atlas", "Cronos", "Prometheus", "Morpheus", "Helios", "Eros",
        "Osiris", "Horus", "Anubis", "Ra", "Amon", "Ptah", "Thot", "Khonsou", "Geb", "Shou",
        "Ramses", "Kheops", "Toutankhamon", "Akhenaton", "Imhotep", "Mentouhotep", "Seti", "Narmer", "Djoser", "Menkauhor",
        "Loki", "Thor", "Odin", "Balder", "Tyr", "Freyr", "Heimdall", "Vidar", "Bragi", "Njord",
        "Luffy", "Zoro", "Sanji", "Usopp", "Franky", "Brook", "Jinbe", "Ace", "Sabo", "Law",
        "Kid", "Killer", "Shanks", "Roger", "Rayleigh", "Mihawk", "Crocodile", "Doflamingo", "Katakuri", "Kaido",
        "BarbeBlanche", "Oden", "Smoker", "Koby", "Garp", "Sengoku", "Fujitora", "Aokiji", "Kizaru", "Ryokugyu",
        "Enel", "Arlong", "Buggy", "Lucci", "Teach", "Marco", "King", "Queen", "Yasopp", "Denjiro",
        "Ryuma", "Zunesh", "Neptune", "Momonosuke", "Kinemon", "Pedro", "Inuarashi", "Nekomamushi", "Kalgara", "Noland"
    );

    private static final List<String> FEMALE_NAMES = List.of(
        "Athena", "Hera", "Artemis", "Aphrodite", "Demeter", "Persephone", "Hestia", "Gaia", "Selene", "Nyx",
        "Eos", "Themis", "Metis", "Iris", "Calliope", "Clio", "Erato", "Thalia", "Urania", "Andromeda",
        "Isis", "Bastet", "Sekhmet", "Nephthys", "Maat", "Hathor", "Nout", "Tefnout", "Satet", "Anuket",
        "Nefertiti", "Nefertari", "Cleopatre", "Meritamen", "Tiye", "Ahmes", "Hatshepsout", "Sobekneferou", "Ankhesenamon", "Kiya",
        "Frigg", "Freyja", "Sif", "Idunn", "Skadi", "Hel", "Eir", "Ran", "Sigyn", "Nanna",
        "Nami", "Robin", "Vivi", "Boa", "Yamato", "Tashigi", "Shirahoshi", "Koala", "Rebecca", "Perona",
        "Carrot", "Ulti", "Hiyori", "Nojiko", "Makino", "Kalifa", "Monet", "Viola", "Pudding", "Reiju",
        "Hancock", "Toki", "Kiku", "Camie", "Conis", "Aisa", "Marguerite", "BeloBetty", "Bonney", "Stussy",
        "Lilin", "Sadi", "Amande", "Brulee", "Smoothie", "WhiteyBay", "Moda", "Kaya", "Alvida", "Tsuru",
        "Olvia", "Lilith", "York", "AtlasOP", "Mousse", "Porche", "Domino", "Aphro", "RanOP", "Bellmere"
    );

    private static final List<String> UNKNOWN_GENDER_NAMES = List.of(
        "Sphinx", "Phoenix", "Typhon", "Lotus", "Papyrus", "Nile", "Kemet", "Abydos", "Memphis", "Gizeh",
        "Olympe", "Styx", "Acheron", "Delphes", "Olympia", "Argos", "Sparta", "Thebes", "Corinthe", "Rhodes",
        "Valhalla", "Asgard", "Midgard", "Bifrost", "Yggdrasil", "Ragnarok", "Skoll", "Hati", "Fenrir", "Jormungand",
        "Sunny", "Merry", "ThousandSunny", "RedForce", "MobyDick", "Pluton", "Uranus", "PoseidonOP", "Poneglyphe", "LaughTale",
        "Wano", "Alabasta", "Skypiea", "Dressrosa", "Zou", "Elbaf", "FishmanIsland", "PunkHazard", "Egghead", "Sabaody",
        "ImpelDown", "Marineford", "EniesLobby", "AmazonLily", "Loguetown", "WhiskeyPeak", "Jaya", "WaterSeven", "Onigashima", "Raftel",
        "Ankh", "Scarabee", "Obelisque", "Uraeus", "Canope", "KaStone", "Benben", "Djed", "Tyet", "Shen",
        "Aegis", "Trident", "Ambrosia", "Nectar", "Chimere", "Hydre", "Minotaure", "Pegase", "Cerbere", "Satyre",
        "Boreas", "Notos", "Eurus", "Zephyrus", "Eclipse", "Orakel", "Pharaon", "Nomos", "SoleilNoir", "Aurore",
        "Nebuleuse", "Comete", "Mirage", "Sirocco", "Nereide", "Asterion", "Ilios", "Cedar", "Rune", "Echo"
    );

    private RandomNameProvider() {
    }

    public static String getRandomMaleName() {
        return MALE_NAMES.get(ThreadLocalRandom.current().nextInt(MALE_NAMES.size()));
    }

    public static String getRandomMaleName(Set<String> usedNames) {
        return getRandomUniqueName(MALE_NAMES, usedNames);
    }

    public static String getRandomFemaleName() {
        return FEMALE_NAMES.get(ThreadLocalRandom.current().nextInt(FEMALE_NAMES.size()));
    }

    public static String getRandomFemaleName(Set<String> usedNames) {
        return getRandomUniqueName(FEMALE_NAMES, usedNames);
    }

    public static String getRandomUnknownGenderName() {
        return UNKNOWN_GENDER_NAMES.get(ThreadLocalRandom.current().nextInt(UNKNOWN_GENDER_NAMES.size()));
    }

    public static String getRandomUnknownGenderName(Set<String> usedNames) {
        return getRandomUniqueName(UNKNOWN_GENDER_NAMES, usedNames);
    }

    private static String getRandomUniqueName(List<String> nameBank, Set<String> usedNames) {
        if (usedNames == null) {
            throw new IllegalArgumentException("usedNames ne peut pas être null");
        }

        if (usedNames.size() >= nameBank.size()) {
            throw new IllegalStateException("Plus de noms disponibles dans la banque");
        }

        List<String> availableNames = new ArrayList<>(nameBank.size());
        for (String name : nameBank) {
            if (!usedNames.contains(name)) {
                availableNames.add(name);
            }
        }

        String selected = availableNames.get(ThreadLocalRandom.current().nextInt(availableNames.size()));
        usedNames.add(selected);
        return selected;
    }
}

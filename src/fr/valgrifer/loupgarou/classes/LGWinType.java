package fr.valgrifer.loupgarou.classes;

import lombok.Getter;
import static org.bukkit.ChatColor.*;

import java.util.ArrayList;
import java.util.Objects;

public class LGWinType {
    @Getter
    private static final ArrayList<LGWinType> values = new ArrayList<>();
    public static LGWinType register(String name, String message)
    {
        LGWinType cause;
        if((cause = getWinType(name)) != null)
            return cause;
        return new LGWinType(name, message);
    }

    public static LGWinType getWinType(String name)
    {
        for (LGWinType cause : getValues())
            if(cause.getName().equalsIgnoreCase(name))
                return cause;
        return null;
    }

    public static LGWinType NONE = register("NONE", DARK_RED+"Erreur: "+RED+"personne n'a gagné la partie.");
    public static LGWinType EQUAL = register("EQUAL", GRAY+""+BOLD+""+ITALIC+"Égalité"+GOLD+""+BOLD+""+ITALIC+", personne n'a gagné la partie !");
    public static LGWinType VILLAGEOIS = register("VILLAGEOIS", GOLD+""+BOLD+""+ITALIC+"La partie a été gagnée par le "+DARK_GREEN+""+BOLD+"Village"+GOLD+""+BOLD+""+ITALIC+" !");
    public static LGWinType LOUPGAROU = register("LOUPGAROU", GOLD+""+BOLD+""+ITALIC+"La partie a été gagnée par les "+RED+""+BOLD+"Loups-Garous"+GOLD+""+BOLD+""+ITALIC+" !");
    public static LGWinType VAMPIRE = register("VAMPIRE", GOLD+""+BOLD+""+ITALIC+"La partie a été gagnée par les "+DARK_PURPLE+""+BOLD+"Vampires"+GOLD+""+BOLD+""+ITALIC+" !");
    public static LGWinType SOLO = register("SOLO", GOLD+""+BOLD+""+ITALIC+"Un joueur solitaire a gagné la partie!");

    public static LGWinType COUPLE = register("COUPLE", GOLD+""+BOLD+""+ITALIC+"La partie a été gagnée par le "+LIGHT_PURPLE+""+BOLD+"couple"+GOLD+""+BOLD+""+ITALIC+" !");
    public static LGWinType LOUPGAROUBLANC = register("LOUPGAROUBLANC", GOLD+""+BOLD+""+ITALIC+"La partie a été gagnée par le "+RED+""+BOLD+"Loup-Garou Blanc"+GOLD+""+BOLD+""+ITALIC+" !");
    public static LGWinType ANGE = register("ANGE", GOLD+""+BOLD+""+ITALIC+"La partie a été gagnée par l'"+LIGHT_PURPLE+""+BOLD+"Ange"+GOLD+""+BOLD+""+ITALIC+" !");
    public static LGWinType ASSASSIN = register("ASSASSIN", GOLD+""+BOLD+""+ITALIC+"La partie a été gagnée par l'"+DARK_BLUE+""+BOLD+"Assassin"+GOLD+""+BOLD+""+ITALIC+" !");
    public static LGWinType PYROMANE = register("PYROMANE", GOLD+""+BOLD+""+ITALIC+"La partie a été gagnée par le "+GOLD+""+BOLD+"Pyromane"+GOLD+""+BOLD+""+ITALIC+" !");

    @Getter
    private final String name;
    @Getter
    private final String message;
    private LGWinType(String name, String message) {
        this.name = name.replaceAll("[^\\w]", "");
        this.message = message;

        values.add(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{name='" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LGWinType that = (LGWinType) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

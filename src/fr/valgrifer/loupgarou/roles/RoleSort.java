package fr.valgrifer.loupgarou.roles;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

public class RoleSort {
    @Getter
    private static final ArrayList<RoleSort> values = new ArrayList<>();
    public static RoleSort registerAt(Class<? extends Role> clazz, int index)
    {
        return registerAt(clazz.getSimpleName().substring(1), index);
    }
    public static RoleSort registerAt(String name, int index)
    {
        RoleSort rolesort;
        if((rolesort = getRoleSort(name)) != null)
            return rolesort;
        rolesort = new RoleSort(name);
        if(index == -1)
            values.add(rolesort);
        else
            values.add(index, rolesort);
        return rolesort;
    }
    public static RoleSort register(Class<? extends Role> clazz)
    {
        return registerAt(clazz.getSimpleName().substring(1), -1);
    }
    public static RoleSort register(String name)
    {
        return registerAt(name, -1);
    }
    public static RoleSort registerBefore(Class<? extends Role> clazz, Class<? extends Role> at)
    {
        return registerBefore(clazz.getSimpleName().substring(1), at.getSimpleName().substring(1));
    }
    public static RoleSort registerBefore(String name, String at)
    {
        return registerAt(name, indexOfRoleSort(at));
    }
    public static RoleSort registerAfter(Class<? extends Role> clazz, Class<? extends Role> at)
    {
        return registerAfter(clazz.getSimpleName().substring(1), at.getSimpleName().substring(1));
    }
    public static RoleSort registerAfter(String name, String at)
    {
        int index = indexOfRoleSort(at);
        if(index >= 0)
            index++;
        return registerAt(name, index);
    }

    public static RoleSort getRoleSort(String name)
    {
        for (RoleSort rolesort : getValues())
            if(rolesort.getName().equalsIgnoreCase(name))
                return rolesort;
        return null;
    }

    public static int indexOfRoleSort(String name)
    {
        return values.indexOf(getRoleSort(name));
    }


    public static final RoleSort ChienLoup;
    public static final RoleSort EnfantSauvage;
    public static final RoleSort AngelV2;
    public static final RoleSort Cupidon;
    public static final RoleSort AngelV2Guardian;
    public static final RoleSort AngelV2Fallen;
    public static final RoleSort Garde;
    public static final RoleSort Survivant;
    public static final RoleSort Voyante;
    public static final RoleSort Detective;
    public static final RoleSort Dictateur;
    public static final RoleSort Pretre;
    public static final RoleSort LoupGarou;
    public static final RoleSort LoupGarouNoir;
    public static final RoleSort GrandMechantLoup;
    public static final RoleSort LoupGarouBlanc;
    public static final RoleSort Assassin;
    public static final RoleSort Pyromane;
    public static final RoleSort ChasseurDeVampire;
    public static final RoleSort Vampire;
    public static final RoleSort Pirate;
    public static final RoleSort Bouffon;
    public static final RoleSort Sorciere;
    public static final RoleSort Corbeau;

    static {
        ChienLoup = register(RDogWolf.class);
        EnfantSauvage = register(RChildWild.class);
        AngelV2 = register(RAngelV2.class);
        Cupidon = register(RCupid.class);
        AngelV2Guardian = register(RAngelV2Guardian.class);
        AngelV2Fallen = register(RAngelV2Fallen.class);
        Garde = register(RGardien.class);
        Survivant = register(RSurvivor.class);
        Voyante = register(RClairvoyant.class);
        Detective = register(RDetective.class);
        Dictateur = register(RDictator.class);
        Pretre = register(RPriest.class);
        LoupGarou = register(RWereWolf.class);
        LoupGarouNoir = register(RBlackWerewolf.class);
        GrandMechantLoup = register(RBigBadWolf.class);
        LoupGarouBlanc = register(RWhiteWerewolf.class);
        Assassin = register(RAssassin.class);
        Pyromane = register(RPyromaniac.class);
        ChasseurDeVampire = register(RVampireHunter.class);
        Vampire = register(RVampire.class);
        Pirate = register(RPirate.class);
        Bouffon = register(RJester.class);
        Sorciere = register(RWitch.class);
        Corbeau = register(RRaven.class);
    }
    

    @Getter
    private final String name;
    private RoleSort(String name) {
        this.name = name.replaceAll("\\W", "");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{name='" + name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleSort that = (RoleSort) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

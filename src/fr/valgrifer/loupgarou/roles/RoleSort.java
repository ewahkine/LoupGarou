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
    
    
    public static final RoleSort ChienLoup = register(RChienLoup.class);
    public static final RoleSort EnfantSauvage = register(REnfantSauvage.class);
    public static final RoleSort Cupidon = register(RCupidon.class);
    public static final RoleSort Garde = register(RGarde.class);
    public static final RoleSort Survivant = register(RSurvivant.class);
    public static final RoleSort Voyante = register(RVoyante.class);
    public static final RoleSort Detective = register(RDetective.class);
    public static final RoleSort Dictateur = register(RDictateur.class);
    public static final RoleSort Pretre = register(RPretre.class);
    public static final RoleSort LoupGarou = register(RLoupGarou.class);
    public static final RoleSort LoupGarouNoir = register(RLoupGarouNoir.class);
    public static final RoleSort GrandMechantLoup = register(RGrandMechantLoup.class);
    public static final RoleSort LoupGarouBlanc = register(RLoupGarouBlanc.class);
    public static final RoleSort Assassin = register(RAssassin.class);
    public static final RoleSort Pyromane = register(RPyromane.class);
    public static final RoleSort ChasseurDeVampire = register(RChasseurDeVampire.class);
    public static final RoleSort Vampire = register(RVampire.class);
    public static final RoleSort Pirate = register(RPirate.class);
    public static final RoleSort Bouffon = register(RBouffon.class);
    public static final RoleSort Sorciere = register(RSorciere.class);
    public static final RoleSort Corbeau = register(RCorbeau.class);
    

    @Getter
    private final String name;
    private RoleSort(String name) {
        this.name = name.replaceAll("[^\\w]", "");
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

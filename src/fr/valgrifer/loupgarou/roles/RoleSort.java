package fr.valgrifer.loupgarou.roles;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

public class RoleSort {
    @Getter
    private static final ArrayList<RoleSort> values = new ArrayList<>();
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
    public static RoleSort register(String name)
    {
        return registerAt(name, -1);
    }
    public static RoleSort registerBefore(String name, String name2)
    {
        return registerAt(name, indexOfRoleSort(name2));
    }
    public static RoleSort registerAfter(String name, String name2)
    {
        int index = indexOfRoleSort(name2);
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
    
    
    public static final RoleSort ChienLoup = register("ChienLoup");
    public static final RoleSort EnfantSauvage = register("EnfantSauvage");
    public static final RoleSort Cupidon = register("Cupidon");
    public static final RoleSort Garde = register("Garde");
    public static final RoleSort Survivant = register("Survivant");
    public static final RoleSort Voyante = register("Voyante");
    public static final RoleSort Detective = register("Detective");
    public static final RoleSort Dictateur = register("Dictateur");
    public static final RoleSort Pretre = register("Pretre");
    public static final RoleSort LoupGarou = register("LoupGarou");
    public static final RoleSort LoupGarouNoir = register("LoupGarouNoir");
    public static final RoleSort GrandMechantLoup = register("GrandMechantLoup");
    public static final RoleSort LoupGarouBlanc = register("LoupGarouBlanc");
    public static final RoleSort Assassin = register("Assassin");
    public static final RoleSort Pyromane = register("Pyromane");
    public static final RoleSort ChasseurDeVampire = register("ChasseurDeVampire");
    public static final RoleSort Vampire = register("Vampire");
    public static final RoleSort Pirate = register("Pirate");
    public static final RoleSort Bouffon = register("Bouffon");
    public static final RoleSort Sorciere = register("Sorciere");
    public static final RoleSort Corbeau = register("Corbeau");
    

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

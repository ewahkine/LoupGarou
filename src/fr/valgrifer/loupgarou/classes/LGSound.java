package fr.valgrifer.loupgarou.classes;

import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import java.util.ArrayList;
import java.util.Objects;

public class LGSound {
    @Getter
    private static final ArrayList<LGSound> values = new ArrayList<>();

    public static LGSound register(String name, Sound s, SoundCategory c)
    {
        LGSound sound;
        if ((sound = getSound(name)) != null)
            return sound;
        return new LGSound(name, s, c);
    }

    public static LGSound getSound(String name)
    {
        for (LGSound sound : getValues())
            if (sound.getName().equalsIgnoreCase(name))
                return sound;
        return null;
    }


    public static final LGSound KILL = register("KILL", Sound.ENTITY_BLAZE_DEATH, SoundCategory.PLAYERS);
    public static final LGSound START_NIGHT = register("START_NIGHT", Sound.ENTITY_SKELETON_DEATH, SoundCategory.MUSIC);
    public static final LGSound START_DAY = register("START_DAY", Sound.ENTITY_ZOMBIE_DEATH, SoundCategory.MUSIC);
    public static final LGSound AMBIANT_NIGHT = register("AMBIANT_NIGHT", Sound.MUSIC_DISC_MALL, SoundCategory.AMBIENT);
    public static final LGSound AMBIANT_DAY = register("AMBIANT_DAY", Sound.MUSIC_DISC_MELLOHI, SoundCategory.AMBIENT);
    public static final LGSound Button = LGSound.register("BUTTON", Sound.UI_BUTTON_CLICK, SoundCategory.MASTER);

    @Getter
    private final String name;
    @Getter
    private final Sound sound;
    @Getter
    private final SoundCategory category;

    private LGSound(String name, Sound sound, SoundCategory category)
    {
        this.name = name.replaceAll("[^\\w]", "");

        this.sound = sound;
        this.category = category;
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
        LGSound that = (LGSound) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

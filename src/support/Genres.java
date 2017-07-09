package support;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

// TODO convert to enum
public class Genres
{
    private static HashMap<String, Integer> nameToId;
    
    private static void initializeMapping()
    {
        nameToId = new HashMap<>();
        nameToId.put("Blues", 0);
        nameToId.put("Classic Rock", 1);
        nameToId.put("Country", 2);
        nameToId.put("Dance", 3);
        nameToId.put("Disco", 4);
        nameToId.put("Funk", 5);
        nameToId.put("Grunge", 6);
        nameToId.put("Hip-Hop", 7);
        nameToId.put("Jazz", 8);
        nameToId.put("Metal", 9);
        nameToId.put("New Age", 10);
        nameToId.put("Oldies", 11);
        nameToId.put("Other", 12);
        nameToId.put("Pop", 13);
        nameToId.put("R&B", 14);
        nameToId.put("Rap", 15);
        nameToId.put("Reggae", 16);
        nameToId.put("Rock", 17);
        nameToId.put("Techno", 18);
        nameToId.put("Industrial", 19);
        nameToId.put("Alternative", 20);
        nameToId.put("Ska", 21);
        nameToId.put("Death Metal", 22);
        nameToId.put("Pranks", 23);
        nameToId.put("Soundtrack", 24);
        nameToId.put("Euro-Techno", 25);
        nameToId.put("Ambient", 26);
        nameToId.put("Trip-Hop", 27);
        nameToId.put("Vocal", 28);
        nameToId.put("Jazz+Funk", 29);
        nameToId.put("Fusion", 30);
        nameToId.put("Trance", 31);
        nameToId.put("Classical", 32);
        nameToId.put("Instrumental", 33);
        nameToId.put("Acid", 34);
        nameToId.put("House", 35);
        nameToId.put("Game", 36);
        nameToId.put("Sound Clip", 37);
        nameToId.put("Gospel", 38);
        nameToId.put("Noise", 39);
        nameToId.put("Alternative Rock", 40);
        nameToId.put("Bass", 41);
        nameToId.put("Soul", 42);
        nameToId.put("Punk", 43);
        nameToId.put("Space", 44);
        nameToId.put("Meditative", 45);
        nameToId.put("Instrumental Pop", 46);
        nameToId.put("Instrumental Rock", 47);
        nameToId.put("Ethnic", 48);
        nameToId.put("Gothic", 49);
        nameToId.put("Darkwave", 50);
        nameToId.put("Techno-Industrial", 51);
        nameToId.put("Electronic", 52);
        nameToId.put("Pop-Folk", 53);
        nameToId.put("Eurodance", 54);
        nameToId.put("Dream", 55);
        nameToId.put("Southern Rock", 56);
        nameToId.put("Comedy", 57);
        nameToId.put("Cult", 58);
        nameToId.put("Gangsta", 59);
        nameToId.put("Top 40", 60);
        nameToId.put("Christian Rap", 61);
        nameToId.put("Pop/Funk", 62);
        nameToId.put("Jungle", 63);
        nameToId.put("Native US", 64);
        nameToId.put("Cabaret", 65);
        nameToId.put("New Wave", 66);
        nameToId.put("Psychadelic", 67);
        nameToId.put("Rave", 68);
        nameToId.put("Showtunes", 69);
        nameToId.put("Trailer", 70);
        nameToId.put("Lo-Fi", 71);
        nameToId.put("Tribal", 72);
        nameToId.put("Acid Punk", 73);
        nameToId.put("Acid Jazz", 74);
        nameToId.put("Polka", 75);
        nameToId.put("Retro", 76);
        nameToId.put("Musical", 77);
        nameToId.put("Rock & Roll", 78);
        nameToId.put("Hard Rock", 79);
        nameToId.put("Folk", 80);
        nameToId.put("Folk-Rock", 81);
        nameToId.put("National Folk", 82);
        nameToId.put("Swing", 83);
        nameToId.put("Fast Fusion", 84);
        nameToId.put("Bebob", 85);
        nameToId.put("Latin", 86);
        nameToId.put("Revival", 87);
        nameToId.put("Celtic", 88);
        nameToId.put("Bluegrass", 89);
        nameToId.put("Avantgarde", 90);
        nameToId.put("Gothic Rock", 91);
        nameToId.put("Progressive Rock", 92);
        nameToId.put("Psychedelic Rock", 93);
        nameToId.put("Symphonic Rock", 94);
        nameToId.put("Slow Rock", 95);
        nameToId.put("Big Band", 96);
        nameToId.put("Chorus", 97);
        nameToId.put("Easy Listening", 98);
        nameToId.put("Acoustic", 99);
        nameToId.put("Humour", 100);
        nameToId.put("Speech", 101);
        nameToId.put("Chanson", 102);
        nameToId.put("Opera", 103);
        nameToId.put("Chamber Music", 104);
        nameToId.put("Sonata", 105);
        nameToId.put("Symphony", 106);
        nameToId.put("Booty Bass", 107);
        nameToId.put("Primus", 108);
        nameToId.put("Porn Groove", 109);
        nameToId.put("Satire", 110);
        nameToId.put("Slow Jam", 111);
        nameToId.put("Club", 112);
        nameToId.put("Tango", 113);
        nameToId.put("Samba", 114);
        nameToId.put("Folklore", 115);
        nameToId.put("Ballad", 116);
        nameToId.put("Power Ballad", 117);
        nameToId.put("Rhytmic Soul", 118);
        nameToId.put("Freestyle", 119);
        nameToId.put("Duet", 120);
        nameToId.put("Punk Rock", 121);
        nameToId.put("Drum Solo", 122);
        nameToId.put("Acapella", 123);
        nameToId.put("Euro-House", 124);
        nameToId.put("Dance Hall", 125);
        nameToId.put("Goa", 126);
        nameToId.put("Drum & Bass", 127);
        nameToId.put("Club-House", 128);
        nameToId.put("Hardcore", 129);
        nameToId.put("Terror", 130);
        nameToId.put("Indie", 131);
        nameToId.put("BritPop", 132);
        nameToId.put("Negerpunk", 133);
        nameToId.put("Polsk Punk", 134);
        nameToId.put("Beat", 135);
        nameToId.put("Christian Gangsta", 136);
        nameToId.put("Heavy Metal", 137);
        nameToId.put("Black Metal", 138);
        nameToId.put("Crossover", 139);
        nameToId.put("Contemporary C", 140);
        nameToId.put("Christian Rock", 141);
        nameToId.put("Merengue", 142);
        nameToId.put("Salsa", 143);
        nameToId.put("Thrash Metal", 144);
        nameToId.put("Anime", 145);
        nameToId.put("JPop", 146);
        nameToId.put("SynthPop", 147);
    }
    
    public static int getIDForName(String name)
    {
        if(nameToId == null)
        {
            initializeMapping();
        }
        Integer id = nameToId.get(name);
        return id == null ? -1 : id;
    }
    
    public static List<String> containsIgnoreCase(String name)
    {
        List<String> possibles = new ArrayList<>();
        if(nameToId == null)
        {
            initializeMapping();
        }
        for(String str : nameToId.keySet())
        {
            if(str.toLowerCase().contains(name.toLowerCase()))
            {
                possibles.add(str);
            }
        }
        possibles.sort(new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareToIgnoreCase(o2);
            }
            
        });
        return possibles;
    }

}

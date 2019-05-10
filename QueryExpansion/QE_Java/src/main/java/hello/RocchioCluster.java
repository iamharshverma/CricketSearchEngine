


package hello;

        import java.io.IOException;
        import java.io.StringReader;
        import java.util.*;
        import java.util.stream.Collectors;

public class RocchioCluster {
    private String query;
    private MyResponse myres;
    private float alpha=1;
    private double beta=.75;


    public static String[] stopwords = {"a","A", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully","b","B", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by","c","C", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently","d","D", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during","e","E", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "f","F","far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "g","G","get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings","h","H", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i","I", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself","j","J", "just", "k","K","keep", "keeps", "kept", "know", "knows", "known", "l","L","last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "m","M","mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself","n","N", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "o","O","of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "p","P","particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "q","Q","que", "quite", "qv","r","R", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right","s","S", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure","t","T", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the","The", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "u","U","un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "v","V","value", "various", "very", "via", "viz", "vs", "w","W","want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt","x","X","y","Y", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};
    public static Set<String> stopWordSet = new HashSet<String>(Arrays.asList(stopwords));

    public RocchioCluster(String myquery, MyResponse myre) {
        this.myres = myre;
        this.query = myquery;
    }

    public String getExpandedQuery() {
        HashMap<String,Integer> vocab= new HashMap<>();
        HashMap<Integer,QueryDoc> doc_vocab_freq=new HashMap<>();
        String[] tokens;
        int count=0;
        for (Doc singledoc : myres.response.docs)
        {
            singledoc.content=singledoc.content.replaceAll("[^A-Za-z]+", " ");
            tokens = singledoc.content.split(" ");
            count++;
            doc_vocab_freq.put(count,new QueryDoc());
            QueryDoc q_doc = doc_vocab_freq.get(count);
            for(int i=0;i<tokens.length;i++)
            {   if(!stopWordSet.contains(tokens[i]))
            {
                if(vocab.containsKey(tokens[i])){
                    int freq = vocab.get(tokens[i]);
                    freq += 1;
                    vocab.remove(tokens[i]);
                    vocab.put(tokens[i], freq);
                }
                else{
                    vocab.put(tokens[i],1);
                }
                if(q_doc.stems.containsKey(tokens[i])){
                    int freq = q_doc.stems.get(tokens[i]);
                    freq += 1;
                    q_doc.stems.remove(tokens[i]);
                    q_doc.stems.put(tokens[i], freq);
                }
                else{
                    q_doc.stems.put(tokens[i],1);
                }
            }

            }
        }
        HashMap<String, ClusterStructure> mapped_cluster_structure = new HashMap<String, ClusterStructure>();
        HashMap<String, ClusterStructure> mapped_cluster_structure_norm = new HashMap<String, ClusterStructure>();
        for (Map.Entry<Integer,QueryDoc> document : doc_vocab_freq.entrySet()){
            for (Map.Entry<String,Integer> term : vocab.entrySet()){
                for (Map.Entry<String,Integer> co_term : vocab.entrySet()){
                    if(document.getValue().stems.containsKey(term.getKey())&&document.getValue().stems.containsKey(co_term.getKey())){
                        float term_freq = document.getValue().stems.get(term.getKey());
                        float co_term_freq = document.getValue().stems.get(co_term.getKey());
                        float mult_value = term_freq*co_term_freq;
                        if(mapped_cluster_structure.containsKey(term.getKey()+","+co_term.getKey())){
                            float get_freq = mapped_cluster_structure.get(term.getKey()+","+co_term.getKey()).value;
                            get_freq+=mult_value;
                            mapped_cluster_structure.put(term.getKey()+","+co_term.getKey(),new ClusterStructure(co_term.getKey(),term.getKey(),get_freq));
                        }
                        else{
                            mapped_cluster_structure.put(term.getKey()+","+co_term.getKey(),new ClusterStructure(co_term.getKey(),term.getKey(),mult_value));
                        }
                    }

                }

            }
        }
        String[] query_tokens = query.split(" ");
        for(int i=0;i<query_tokens.length;i++)
        {
            for (Map.Entry<String, Integer> term : vocab.entrySet()) {
                if(mapped_cluster_structure.containsKey(query_tokens[i]+","+term.getKey())){
                    float term_coterm=mapped_cluster_structure.get(query_tokens[i]+","+term.getKey()).value;
                    float coterm_coterm=mapped_cluster_structure.get(term.getKey()+","+term.getKey()).value;
                    float term_term=mapped_cluster_structure.get(query_tokens[i]+","+query_tokens[i]).value;
                    float score = term_coterm/(term_coterm+term_term+coterm_coterm);
                    mapped_cluster_structure_norm.put(query_tokens[i]+","+term.getKey(),new ClusterStructure(term.getKey(),query_tokens[i],score));
                }

            }
        }

        Comparator<Map.Entry<String, ClusterStructure>> Cluster_Struct_Comparator = new Comparator<Map.Entry<String, ClusterStructure>>() {
            @Override
            public int compare(Map.Entry<String, ClusterStructure> o1, Map.Entry<String, ClusterStructure> o2) {
                ClusterStructure obj1 = o1.getValue();
                ClusterStructure obj2 = o2.getValue();
                if (obj1.value > obj2.value)
                    return -1;
                else if (obj2.value > obj1.value)
                    return 1;
                else
                    return 0;
            }
        };
        LinkedHashMap<String, ClusterStructure> sorted_cluster_structure_map_norm;
        StringBuilder sb = new StringBuilder();
        sorted_cluster_structure_map_norm = mapped_cluster_structure_norm.entrySet().stream().sorted(Cluster_Struct_Comparator).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        Set<String> queryset = new HashSet<String>(Arrays.asList(query_tokens));
        Set<String> stringset = new HashSet<String>();
        for(int i=0;i<query_tokens.length;i++)
        {
            sb.append(query_tokens[i]+",");
            int count1=0;
            for (Map.Entry<String,ClusterStructure> sorted_struct : sorted_cluster_structure_map_norm.entrySet()) {
                if((sorted_struct.getKey().compareTo(query_tokens[i]+","+query_tokens[i]))!=0&&!(queryset.contains(sorted_struct.getValue().term2)))
                {



                        if(!stringset.contains(sorted_struct.getValue().term2)) {
                            count1++;

                            if (count1 > 3 && count1 < 7) {

                                stringset.add(sorted_struct.getValue().term2);
                            }
                        }

                }
            }
        }

        Iterator<String> it = stringset.iterator();
        while(it.hasNext()) {
            sb.append(it.next()+",");
        }
        String expanded_Query= sb.toString();


        return expanded_Query;
    }
}

package ma.emsi.ketoun.tp4_web_aboubakr_ketoun_19.RAG;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EmbeddingStoreCreator {

    public static EmbeddingStore<TextSegment> creerEmbeddingStore(String cheminRessource, EmbeddingModel embeddingModel) {
        Path pathRessource = getPathRessource(cheminRessource);

        DocumentParser documentParser = new ApacheTikaDocumentParser();

        Document document = FileSystemDocumentLoader.loadDocument(pathRessource, documentParser);

        DocumentSplitter documentSplitter = DocumentSplitters.recursive(300, 30);

        List<TextSegment> segments = documentSplitter.split(document);

        Response<List<Embedding>> embeddingsResponse = embeddingModel.embedAll(segments);
        List<Embedding> embeddings = embeddingsResponse.content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        embeddingStore.addAll(embeddings, segments);

        return embeddingStore;
    }

    private static Path getPathRessource(String cheminRessource) {
        Path pathRessource;
        try {
            URL fileUrl = EmbeddingStoreCreator.class.getResource(cheminRessource);
            if (fileUrl == null) {
                throw new RuntimeException("Impossible de trouver le fichier " + cheminRessource);
            }
            pathRessource = Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return pathRessource;
    }
}

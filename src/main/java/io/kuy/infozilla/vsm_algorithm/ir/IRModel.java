package io.kuy.infozilla.vsm_algorithm.ir;

import io.kuy.infozilla.vsm_algorithm.document.ArtifactsCollection;
import io.kuy.infozilla.vsm_algorithm.document.SimilarityMatrix;
import io.kuy.infozilla.vsm_algorithm.document.TermDocumentMatrix;

public interface IRModel {
    public SimilarityMatrix Compute(ArtifactsCollection source, ArtifactsCollection target);

//    public SimilarityMatrix Compute(ArtifactsCollection source, ArtifactsCollection target);

    public TermDocumentMatrix getTermDocumentMatrixOfQueries();

    public TermDocumentMatrix getTermDocumentMatrixOfDocuments();
}

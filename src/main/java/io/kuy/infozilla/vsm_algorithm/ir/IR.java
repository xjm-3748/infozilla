package io.kuy.infozilla.vsm_algorithm.ir;

import io.kuy.infozilla.vsm_algorithm.document.LinksList;
import io.kuy.infozilla.vsm_algorithm.document.SimilarityMatrix;
import io.kuy.infozilla.vsm_algorithm.document.TextDataset;

public class IR {

    public static LinksList compute(TextDataset textDataset, String modelType) {


        try {
            Class modelTypeClass = Class.forName(modelType);
            IRModel irModel = (IRModel) modelTypeClass.newInstance();
            // 计算得到IR候选追踪矩阵
            SimilarityMatrix similarityMatrix = irModel.Compute(textDataset.getSourceCollection(),
                    textDataset.getTargetCollection());

            //TODO 在得到相似度矩阵后对结果进行处理
//            similarityMatrix.setCutN(10);  //TODO 测试完成后恢复这两行
//            LinksList topTenLink = similarityMatrix.getLinksInCutN();
            LinksList topTenLink = similarityMatrix.allLinks();

            return topTenLink;
        } catch (ClassNotFoundException e) {
            System.out.println("No such IR model exists");
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

}

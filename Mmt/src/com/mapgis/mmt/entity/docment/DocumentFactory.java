package com.mapgis.mmt.entity.docment;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.entity.docment.Document.MimeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2016/12/15.
 * 生成文档对象的工厂方法
 */

public class DocumentFactory {

    private DocumentFactory() {
        throw new AssertionError("Can't create DocumentFactory object");
    }

    /**
     * 创建文档对象
     *
     * @param extensionName 文档扩展名
     * @return 对应的文档对象
     */
    public static Document create(String extensionName) {
        Document document;
        if (TextUtils.isEmpty(extensionName)) {
            document = new FileDocument(MimeType.File);
        } else if (extensionName.equals("apk")) {
            document = new ApkDocument(MimeType.Apk);
        } else if (extensionName.equals("mp4") || extensionName.equals("avi") || extensionName.equals("3gp") || extensionName.equals("rmvb")) {
            document = new VideoDocument(MimeType.Video);
        } else if (extensionName.equals("m4a") || extensionName.equals("mp3") || extensionName.equals("mid") || extensionName.equals("xmf") || extensionName.equals("ogg")
                || extensionName.equals("wav")) {
            document = new AudioDocument(MimeType.Audio);
        } else if (extensionName.equals("jpg") || extensionName.equals("gif") || extensionName.equals("png") || extensionName.equals("jpeg") || extensionName.equals("bmp")) {
            document = new ImageDocument(MimeType.Image);
        } else if (extensionName.equals("txt") || extensionName.equals("log") || extensionName.equals("xml") || extensionName.equals("config")) {
            document = new TxtDocument(MimeType.Txt);
        } else if (extensionName.equals("zip") || extensionName.equals("rar") || extensionName.equals("7z")) {
            document = new ZipDocument(MimeType.Zip);
        } else if(extensionName.equals("doc") || extensionName.equals("docx")){
            document = new DocDocument(MimeType.Doc);
        }else if(extensionName.contains("pdf")){
            document = new PdfDocument(MimeType.Pdf);
        }else if (extensionName.equals("exls") || extensionName.equals("exl") || extensionName.equals("exlm")){
            document = new ExcelDocument(MimeType.Excel);
        }else{
            document = new UnknowDocument(MimeType.Unknow);
        }
        return document;
    }

    /**
     * 将用逗号隔开的文件列表转化成对应的Document对象
     *
     * @param relativePaths 用逗号隔开的文件路径
     * @param parentPath    其父路径
     * @param type          列表中的文件类型
     * @return 返回Document对象集合
     */
    public static List<Document> createDocuments(String relativePaths, String parentPath, MimeType type) {

        try {
            switch (type) {
                case File:
                    return createDocuments(relativePaths, parentPath, FileDocument.class);
                case Doc:
                    return createDocuments(relativePaths, parentPath, DocDocument.class);
                case Txt:
                    return createDocuments(relativePaths, parentPath, TxtDocument.class);
                case Image:
                    return createDocuments(relativePaths, parentPath, ImageDocument.class);
                case Video:
                    return createDocuments(relativePaths, parentPath, VideoDocument.class);
                case Audio:
                    return createDocuments(relativePaths, parentPath, AudioDocument.class);
                case Attach:
                    return createDocuments(relativePaths, parentPath);
                case Zip:
                    return createDocuments(relativePaths, parentPath, ZipDocument.class);
                case Apk:
                    return createDocuments(relativePaths, parentPath, ApkDocument.class);
                case Unknow:
                default:
                    return createDocuments(relativePaths, parentPath, UnknowDocument.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<Document> createDocuments(ArrayList<String> pathList){
        List<Document> docList = new ArrayList<>();
        for (String s : pathList) {
            Document document = createDocument(s);
            docList.add(document);
        }
        return docList;
    }

    public static List<Document> createDocuments(String relativePaths, String parentPath) {
        List<Document> docList = new ArrayList<>();
        for (String s : BaseClassUtil.StringToList(relativePaths, ",")) {
            String path = parentPath + s;
            Document document = createDocument(path);
            docList.add(document);
        }
        return docList;
    }

    @NonNull
    private static Document createDocument(String path) {
        File file;
        String extensionName = FileUtil.getFileExtension(path);
        Document document = create(extensionName);
        file = new File(path);
        document.setPath(file.getAbsolutePath());
        document.setName(file.getName());
        if ("mp4".equals(extensionName)) {
            // 构造预览图对象
            ImageDocument imageDocument = new ImageDocument();
            File imageFile = new File(path.replace("mp4", "jpg"));
            imageDocument.setPath(imageFile.getAbsolutePath());
            imageDocument.setName(imageFile.getName());
            ((VideoDocument) document).setPreImageDocument(imageDocument);
        }
        return document;
    }

    /**
     * 反射得到对应的文档对象
     *
     * @param relativePaths 相对路径
     * @param clazz         对应的对象类型
     * @return Document集合
     */
    private static ArrayList<Document> createDocuments(String relativePaths, String parentPath, Class clazz) {
        ArrayList<Document> docList = new ArrayList<>();
        try {
            if (BaseClassUtil.isNullOrEmptyString(relativePaths) || BaseClassUtil.isNullOrEmptyString(parentPath)) {
                return docList;
            }

            Document document;
            File file;
            // Todo 需要对视频单独处理
            if (VideoDocument.class.getName().equals(clazz.getName())) {
//                ImageDocument imageDocument;
                for (String s : BaseClassUtil.StringToList(relativePaths, ",")) {
                    // 构造其视频对象
                    document = new VideoDocument();
                    file = new File(parentPath + s);
                    initDocument(file, document);
                    // 构造预览图对象
//                    imageDocument = new ImageDocument();
//                    File imageFile = new File(parentPath + s.replace("mp4", "jpg"));
//                    imageDocument.setPath(imageFile.getAbsolutePath());
//                    imageDocument.setName(imageFile.getName());
                    ((VideoDocument) document).setPreImageDocument(
                            (ImageDocument) initDocument(
                                    new File(parentPath + s.replace("mp4", "jpg"))
                                    , new ImageDocument()));

                    docList.add(document);
                }
                return docList;
            }

            for (String s : relativePaths.split(",")) {
                document = (Document) clazz.getDeclaredConstructor().newInstance();
                file = new File(parentPath + s);
                document.setPath(file.getAbsolutePath());
                document.setName(file.getName());
                docList.add(document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docList;
    }

    /**
     * 根据文件对象创建Document对象
     *
     * @param tempFile 文件对象
     * @return Document对象
     */
    public static Document createDocument(File tempFile) {
        String fileExtension = FileUtil.getFileExtension(tempFile);
        Document document = create(fileExtension);
        // 如果是视频还得另外处理
        if (MimeType.Video.getTypeName().equals(document.getMimeTypeName())) {
            String preImagePath = tempFile.getAbsolutePath().replace("mp4", "jpg");
            File imageFile = new File(preImagePath);
            ImageDocument imageDocument = new ImageDocument();
            initDocument(imageFile, imageDocument);
            ((VideoDocument) document).setPreImageDocument(imageDocument);
        }
        return initDocument(tempFile, document);
    }

    /**
     * 根据File对象初始化一个Document对象
     *
     * @param tempFile 文件对象
     * @param document Document对象
     * @return 返回被初始化的Document对象
     */
    private static Document initDocument(File tempFile, Document document) {
        document.setPath(tempFile.getAbsolutePath());
        document.setName(tempFile.getName());
        document.setLastModified(tempFile.lastModified());
        document.setSize(tempFile.length());
        return document;
    }
}

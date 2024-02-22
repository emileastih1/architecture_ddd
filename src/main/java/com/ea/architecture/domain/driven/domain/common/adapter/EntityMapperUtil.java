package com.ea.architecture.domain.driven.domain.common.adapter;

import com.ea.architecture.domain.driven.domain.common.model.UniqueId;
import com.ea.architecture.domain.driven.domain.document.vo.FileSize;
import com.ea.architecture.domain.driven.domain.document.vo.UnitOfMeasurement;
import org.mapstruct.Named;

import java.io.File;
import java.util.stream.Stream;

public interface EntityMapperUtil {
    @Named("mapUniqueIdToLong")
    default long map(UniqueId uid) { return uid.getId();}
    @Named("mapLongToUniqueId")
    default UniqueId map(long uid) { return new UniqueId(uid);}

    default FileSize map(String fileSize){
        if(fileSize != null){
            var fileSizeSplit = fileSize.split(" ");
            return new FileSize(fileSizeSplit[0], UnitOfMeasurement.valueOf(fileSizeSplit[1]));
        }else{
            return null;
        }
    }
    default String map(FileSize fileSize){
        if(fileSize != null){
            return fileSize.size() + File.separator + fileSize.unitOfMeasurement();
        }else{
            return "0";
        }
    }
}
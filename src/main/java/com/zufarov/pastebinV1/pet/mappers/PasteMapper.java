package com.zufarov.pastebinV1.pet.mappers;

import com.zufarov.pastebinV1.pet.dtos.PasteRequestDto;
import com.zufarov.pastebinV1.pet.dtos.PasteResponseDto;
import com.zufarov.pastebinV1.pet.dtos.PasteUpdateDto;
import com.zufarov.pastebinV1.pet.models.Paste;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PasteMapper {
    @Mapping(target = "id",expression = "java(pasteId)")
    @Mapping(target = "createdAt",expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastVisited",expression = "java(java.time.LocalDateTime.now())")
    Paste toPaste(PasteRequestDto pasteRequestDto, String pasteId);

    @Mapping(target = "content",expression = "java(pasteContent)")
    PasteResponseDto toPasteResponseDto(Paste paste,String pasteContent);

    void updatePaste(PasteUpdateDto  pasteUpdateDto,@MappingTarget Paste paste);

}

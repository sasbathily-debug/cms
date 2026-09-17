package com.projet.site.service;

import com.projet.site.domain.FaqItem;
import com.projet.site.dto.FaqItemDto;
import com.projet.site.dto.FaqItemRequest;
import com.projet.site.exception.ResourceNotFoundException;
import com.projet.site.mapper.FaqItemMapper;
import com.projet.site.repository.FaqItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FaqItemServiceImpl implements FaqItemService {

    private final FaqItemRepository faqItemRepository;
    private final FaqItemMapper faqItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FaqItemDto> findAll() {
        return faqItemRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(faqItemMapper::toDto)
                .toList();
    }

    @Override
    public FaqItemDto create(FaqItemRequest request) {
        FaqItem faqItem = new FaqItem(request.question(), request.answer(), request.displayOrder());
        return faqItemMapper.toDto(faqItemRepository.save(faqItem));
    }

    @Override
    public FaqItemDto update(UUID id, FaqItemRequest request) {
        FaqItem faqItem = faqItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ introuvable : " + id));
        faqItem.setQuestion(request.question());
        faqItem.setAnswer(request.answer());
        faqItem.setDisplayOrder(request.displayOrder());
        return faqItemMapper.toDto(faqItemRepository.save(faqItem));
    }

    @Override
    public void delete(UUID id) {
        if (!faqItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("FAQ introuvable : " + id);
        }
        faqItemRepository.deleteById(id);
    }
}

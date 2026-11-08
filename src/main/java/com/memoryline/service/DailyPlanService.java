package com.memoryline.service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import com.memoryline.constant.CardCategory;
import com.memoryline.entity.*;
import com.memoryline.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class DailyPlanService {
    private final DailyPlanRepository plans;
    private final PracticeCardRepository cards;
    private final PracticeLogRepository logs;
    private final CardService cardService;
    private final RecallResultRepository recalls;
    private String today() { return LocalDate.now(ZoneId.of("Asia/Shanghai")).toString(); }
    private List<Long> ids(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        return Arrays.stream(csv.split(",")).map(Long::valueOf).collect(Collectors.toList());
    }
    private String join(List<Long> list) { return list.stream().map(String::valueOf).collect(Collectors.joining(",")); }
    private List<Long> cardIds(List<PracticeCardEntity> list) {
        return list.stream().map(PracticeCardEntity::getId).collect(Collectors.toList());
    }
    private DailyPlanEntity preview(int limit, CardCategory subject, String lesson) {
        if (limit < 1 || limit > 50) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newLimit must be between 1 and 50");
        DailyPlanEntity p = new DailyPlanEntity();
        p.setPlanDate(today()); p.setNewLimit(limit); p.setRecallEnabled(true);
        p.setSubject(subject == null ? "ALL" : subject.name()); p.setLesson(lesson);
        p.setReviewIds(join(cardIds(cardService.getTodayTasks(today(), limit, null, null).getReviewCards())));
        p.setNewIds(join(cardIds(cardService.getTodayTasks(today(), limit, subject, lesson).getNewCards())));
        return p;
    }
    @Transactional(readOnly=true)
    public Map<String,Object> get(int limit, CardCategory subject, String lesson) {
        Optional<DailyPlanEntity> p = plans.findById(today());
        return response(p.orElseGet(() -> preview(limit, subject, lesson)), p.isPresent());
    }
    @Transactional
    public synchronized Map<String,Object> start(int limit, CardCategory subject, String lesson) {
        DailyPlanEntity p = plans.findById(today()).orElseGet(() -> plans.saveAndFlush(preview(limit, subject, lesson)));
        return response(p, true);
    }
    @Transactional
    public synchronized Map<String,Object> extra() {
        DailyPlanEntity p = plans.findById(today()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Start today's plan first"));
        Map<String,Object> state = response(p, true);
        if (!(Boolean) state.get("complete") || !(Boolean) state.get("extraComplete") || ((Number)state.get("recallRemaining")).intValue() > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Finish the current list first");
        Set<Long> used = new HashSet<>();
        used.addAll(ids(p.getReviewIds())); used.addAll(ids(p.getNewIds())); used.addAll(ids(p.getExtraIds()));
        CardCategory category = "ALL".equals(p.getSubject()) ? null : CardCategory.valueOf(p.getSubject());
        List<Long> extra = ids(p.getExtraIds());
        extra.addAll(cardService.getTodayTasks(today(), 50, category, p.getLesson()).getNewCards().stream()
                .map(PracticeCardEntity::getId).filter(id -> !used.contains(id)).limit(5).collect(Collectors.toList()));
        p.setExtraIds(join(extra)); plans.save(p);
        return response(p, true);
    }
    @Transactional
    public synchronized Map<String,Object> recall(Long cardId, Integer rating, boolean usedHint) {
        if(cardId == null || rating == null || rating < 1 || rating > 3)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid recall feedback");
        DailyPlanEntity p=plans.findById(today()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,"No daily plan"));
        String key=today()+":"+cardId;
        if(recalls.existsById(key)) return response(p,true);
        Map<String,Object> state=response(p,true);
        List<PracticeCardEntity> eligible=(List<PracticeCardEntity>)state.get("recallCards");
        if(eligible.stream().noneMatch(c -> cardId.equals(c.getId())))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Recall not ready");
        int effective=usedHint ? Math.min(rating,2) : rating;
        RecallResultEntity result=new RecallResultEntity();
        result.setResultKey(key); result.setRecallDate(today()); result.setCardId(cardId);
        result.setRating(effective); result.setUsedHint(usedHint); recalls.save(result);
        if(effective < 3) {
            PracticeCardEntity card=cards.findById(cardId).orElseThrow();
            card.setNextReviewDate(LocalDate.parse(today()).plusDays(1).toString());
            cards.save(card);
        }
        return response(p,true);
    }
    private Map<String,Object> response(DailyPlanEntity p, boolean started) {
        List<Long> review = ids(p.getReviewIds()), fresh = ids(p.getNewIds()), extra = ids(p.getExtraIds());
        List<PracticeLogEntity> dayLogs=logs.findByReviewDate(p.getPlanDate());
        Set<Long> completed = dayLogs.stream().map(PracticeLogEntity::getCardId).collect(Collectors.toSet());
        List<Long> all = new ArrayList<>(review); all.addAll(fresh); all.addAll(extra);
        Map<Long,PracticeCardEntity> indexed = cards.findAllById(all).stream().collect(Collectors.toMap(PracticeCardEntity::getId,c->c));
        // Deleted cards cannot leave an unfinishable plan.
        review.removeIf(id -> !indexed.containsKey(id)); fresh.removeIf(id -> !indexed.containsKey(id)); extra.removeIf(id -> !indexed.containsKey(id));
        List<Long> base = new ArrayList<>(review); base.addAll(fresh);
        List<Long> queue = new ArrayList<>(base); queue.addAll(extra);
        List<PracticeCardEntity> remaining = queue.stream().filter(id -> !completed.contains(id)).map(indexed::get).filter(Objects::nonNull).collect(Collectors.toList());
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("todayDate",p.getPlanDate()); r.put("started",started);
        r.put("subject",p.getSubject()); r.put("lesson",p.getLesson()); r.put("newLimit",p.getNewLimit());
        r.put("reviewIds",review); r.put("newIds",fresh); r.put("extraIds",extra);
        r.put("reviewDone",review.stream().filter(completed::contains).count());
        r.put("newDone",fresh.stream().filter(completed::contains).count());
        r.put("extraDone",extra.stream().filter(completed::contains).count());
        r.put("completedCount",base.stream().filter(completed::contains).count()); r.put("totalCount",base.size());
        r.put("complete",completed.containsAll(base)); r.put("extraComplete",completed.containsAll(extra));
        r.put("additionalCount",completed.stream().filter(id -> !base.contains(id)).count());
        r.put("remainingCards",remaining);
        // Only plans created after this feature opt in; existing plans retain their finish state.
        List<Long> learned=new ArrayList<>();
        if(started && Boolean.TRUE.equals(p.getRecallEnabled())) {
            Set<Long> newSet=new HashSet<>(fresh);newSet.addAll(extra);
            dayLogs.stream().sorted(Comparator.comparing(PracticeLogEntity::getId))
                .map(PracticeLogEntity::getCardId).filter(indexed::containsKey).filter(id -> !learned.contains(id))
                .forEach(learned::add);
            List<Long> sequence=new ArrayList<>(learned);
            learned.removeIf(id -> !newSet.contains(id));
            Map<Long,RecallResultEntity> results=recalls.findByRecallDate(p.getPlanDate()).stream()
                .collect(Collectors.toMap(RecallResultEntity::getCardId,x->x));
            List<PracticeCardEntity> eligible=learned.stream().filter(id -> !results.containsKey(id))
                .filter(id -> remaining.isEmpty() || sequence.size()-sequence.indexOf(id)-1 >= 2)
                .map(indexed::get).collect(Collectors.toList());
            r.put("recallCards",eligible);
            r.put("recallDone",learned.stream().filter(results::containsKey).count());
            r.put("recallRemaining",learned.stream().filter(id -> !results.containsKey(id)).count());
            r.put("recallNeedsHelp",learned.stream().filter(results::containsKey).filter(id -> results.get(id).getRating()<3).count());
        } else {
            r.put("recallCards",Collections.emptyList());r.put("recallDone",0);r.put("recallRemaining",0);r.put("recallNeedsHelp",0);
        }
        return r;
    }
}

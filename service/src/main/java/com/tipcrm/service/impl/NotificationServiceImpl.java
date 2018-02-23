package com.tipcrm.service.impl;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tipcrm.bo.NotificationBo;
import com.tipcrm.bo.QueryCriteriaBo;
import com.tipcrm.bo.QueryRequestBo;
import com.tipcrm.bo.QueryResultBo;
import com.tipcrm.bo.QuerySortBo;
import com.tipcrm.bo.SimpleNotificationBo;
import com.tipcrm.cache.NotificationCache;
import com.tipcrm.constant.Constants;
import com.tipcrm.constant.ListBoxCategory;
import com.tipcrm.constant.NotificationReadStatus;
import com.tipcrm.constant.NotificationType;
import com.tipcrm.dao.entity.ListBox;
import com.tipcrm.dao.entity.Notification;
import com.tipcrm.dao.repository.ListBoxRepository;
import com.tipcrm.dao.repository.NotificationRepository;
import com.tipcrm.dao.repository.UserRepository;
import com.tipcrm.exception.BizException;
import com.tipcrm.exception.QueryException;
import com.tipcrm.service.NotificationService;
import com.tipcrm.service.UserService;
import com.tipcrm.service.WebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ListBoxRepository listBoxRepository;

    @Autowired
    private WebContext webContext;

    @Autowired
    private NotificationRepository notificationRepository;

    private Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public Integer createNotification(Integer toId, String subject, String content, NotificationType type) {
        Notification notification = new Notification();
        notification.setToUser(userRepository.findOne(toId));
        notification.setSubject(subject);
        notification.setContent(content);
        ListBox notificationType = listBoxRepository.findByCategoryNameAndName(ListBoxCategory.NOTIFICATION_TYPE.name(), type.name());
        ListBox unread = listBoxRepository.findByCategoryNameAndName(ListBoxCategory.NOTIFICATION_READ_STATUS.name(), NotificationReadStatus.UNREAD.name());
        notification.setType(notificationType);
        notification.setReadStatus(unread);
        if (NotificationType.SYSTEM_NOTIFICATION.equals(type)) {
            notification.setEntryUser(userRepository.findByUserName(Constants.User.SYSTEM).get(0));
        } else {
            notification.setEntryUser(webContext.getCurrentUser());
        }
        notification.setEntryTime(new Date());
        notification = notificationRepository.save(notification);
        NotificationCache.push(toId, notification);
        return notification.getId();
    }

    @Override
    public List<SimpleNotificationBo> getMyRealTimeNotifications() {
        return convertToSimpleNotificationBos(NotificationCache.pop(webContext.getCurrentUserId()));
    }

    private List<SimpleNotificationBo> convertToSimpleNotificationBos(List<Notification> notifications) {
        List<SimpleNotificationBo> simpleNotificationBos = Lists.newArrayList();
        if (CollectionUtils.isEmpty(notifications)) {
            return null;
        }
        for (Notification notification : notifications) {
            SimpleNotificationBo simpleNotificationBo = convertToSimpleNotificationBo(notification);
            if (simpleNotificationBo != null) {
                simpleNotificationBos.add(simpleNotificationBo);
            }
        }
        return simpleNotificationBos;
    }

    private SimpleNotificationBo convertToSimpleNotificationBo(Notification notification) {
        if (notification == null) {
            return null;
        }
        SimpleNotificationBo simpleNotificationBo = new SimpleNotificationBo();
        simpleNotificationBo.setFrom(notification.getEntryUser().getUserName());
        simpleNotificationBo.setNotificationId(notification.getId());
        simpleNotificationBo.setSubject(notification.getSubject());
        return simpleNotificationBo;
    }

    private List<NotificationBo> convertToNotificationBos(List<Notification> notifications) {
        List<NotificationBo> notificationBos = Lists.newArrayList();
        if (CollectionUtils.isEmpty(notifications)) {
            return null;
        }
        for (Notification notification : notifications) {
            NotificationBo notificationBo = convertToNotificationBo(notification);
            if (notificationBo != null) {
                notificationBos.add(notificationBo);
            }
        }
        return notificationBos;
    }

    private NotificationBo convertToNotificationBo(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationBo notificationBo = new NotificationBo();
        notificationBo.setSender(notification.getEntryUser().getUserName());
        notificationBo.setId(notification.getId());
        notificationBo.setSubject(notification.getSubject());
        notificationBo.setStatus(notification.getReadStatus().getDisplayName());
        notificationBo.setContent(notification.getContent());
        notificationBo.setTime(notification.getEntryTime());
        return notificationBo;
    }

    @Override
    public QueryResultBo<NotificationBo> getMyNotifications(QueryRequestBo queryRequestBo) throws QueryException, BizException {

        if (CollectionUtils.isEmpty(queryRequestBo.getCriteria())) {
            queryRequestBo.setCriteria(Lists.newArrayList());
        }
        List<QueryCriteriaBo> queryCriteriaBos = queryRequestBo.getCriteria();
        Set<String> fieldSet = Sets.newHashSet();
        for (QueryCriteriaBo queryCriteriaBo : queryCriteriaBos) {
            fieldSet.add(queryCriteriaBo.getFieldName());
        }
        if (fieldSet.size() != queryCriteriaBos.size()) {
            throw new BizException("查询字段有重复");
        }
        NotificationType notificationType = null;
        // find query type
        Iterator<QueryCriteriaBo> it = queryCriteriaBos.iterator();
        while (it.hasNext()) {
            QueryCriteriaBo queryCriteriaBo = it.next();
            if (Constants.QueryFieldName.Notification.TYPE.equals(queryCriteriaBo.getFieldName())) {
                notificationType = NotificationType.valueOf((String) queryCriteriaBo.getValue());
                if (notificationType == null) {
                    throw new BizException("查询条件有误，请检查" + queryCriteriaBo.getValue() + "的拼写");
                }
                it.remove();
                break;
            }
        }
        // find sender
        QueryCriteriaBo queryCriteriaBo = null;
        QueryCriteriaBo toUserCriteriaBo = null;
        for (QueryCriteriaBo criteriaBo : queryCriteriaBos) {
            if (Constants.QueryFieldName.Notification.SENDER.equals(criteriaBo.getFieldName())) {
                queryCriteriaBo = criteriaBo;
            }
            if (Constants.QueryFieldName.Notification.RECEIVER.equals(criteriaBo.getFieldName())) {
                toUserCriteriaBo = criteriaBo;
            }
        }
        if (toUserCriteriaBo == null) {
            toUserCriteriaBo = new QueryCriteriaBo();
            toUserCriteriaBo.setFieldName(Constants.QueryFieldName.Notification.RECEIVER);
            toUserCriteriaBo.setValue(Lists.newArrayList(webContext.getCurrentUserId()));
            queryRequestBo.getCriteria().add(toUserCriteriaBo);
        } else {
            toUserCriteriaBo.setValue(Lists.newArrayList(webContext.getCurrentUserId()));
        }
        Integer systemId = userService.findSystemUser().getId();
        if (NotificationType.SYSTEM_NOTIFICATION.equals(notificationType)) {
            if (queryCriteriaBo == null) {
                queryCriteriaBo = new QueryCriteriaBo();
            }
            queryCriteriaBo.setFieldName(Constants.QueryFieldName.Notification.SENDER);
            queryCriteriaBo.setValue(Lists.newArrayList(systemId));
        } else {
            if (queryCriteriaBo != null) {
                List<Integer> senders = (List<Integer>) queryCriteriaBo.getValue();
                senders.remove(systemId);
            }
        }

        try {
            QuerySortBo querySortBo = queryRequestBo.getSort();
            PageRequest page;
            if (querySortBo == null) {
                page = new PageRequest(queryRequestBo.getPage() - 1, queryRequestBo.getSize());
            } else {
                page = new PageRequest(queryRequestBo.getPage() - 1, queryRequestBo.getSize(),
                                       new Sort(querySortBo.getDirection(),
                                                Constants.SortFieldName.Notification.fieldMap.get(querySortBo.getFieldName())));
            }
            Specification<Notification> specification = new NotificationSpecification(queryRequestBo);
            Page<Notification> notificationPage = notificationRepository.findAll(specification, page);
            List<NotificationBo> notificationBos = convertToNotificationBos(notificationPage.getContent());
            QueryResultBo<NotificationBo> queryResultBo = new QueryResultBo<>(notificationBos, queryRequestBo.getPage(), queryRequestBo.getSize(),
                                                                              notificationPage.getTotalElements(), notificationPage.getTotalPages());
            return queryResultBo;
        } catch (Exception e) {
            throw new QueryException("查询条件错误", e);
        }
    }

    @Override
    public NotificationBo getNotificationById(Integer notificationId) throws BizException {
        Notification notification = notificationRepository.findOne(notificationId);
        if (notification == null) {
            throw new BizException("通知不存在");
        }
        Integer currentUserId = webContext.getCurrentUserId();
        if (!notification.getToUser().getId().equals(currentUserId) &&
            !notification.getEntryUser().getId().equals(currentUserId)) {
            throw new BizException("您没有权限查看该通知");
        }
        return convertToNotificationBo(notification);
    }

    class NotificationSpecification implements Specification<Notification> {
        private QueryRequestBo queryRequestBo;
        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public NotificationSpecification(QueryRequestBo queryRequestBo) {
            this.queryRequestBo = queryRequestBo;
        }

        @Override
        public Predicate toPredicate(Root<Notification> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<Predicate>();
            if (!CollectionUtils.isEmpty(queryRequestBo.getCriteria())) {
                for (QueryCriteriaBo criteria : queryRequestBo.getCriteria()) {
                    Path path = null;
                    switch (criteria.getFieldName()) {
                        case Constants.QueryFieldName.Notification.SENDER:
                            path = root.get("entryUser").get("id");
                            predicates.add(path.in(((List) criteria.getValue()).toArray()));
                            break;
                        case Constants.QueryFieldName.Notification.SEND_TIME:
                            path = root.get("entryTime");
                            try {
                                Date from = sdf.parse((String) ((List) criteria.getValue()).get(0));
                                Date to = sdf.parse((String) ((List) criteria.getValue()).get(1));
                                predicates.add(criteriaBuilder.between(path, from, to));
                            } catch (ParseException e) {
                                logger.error("日期转换失败，略过该查询条件", e);
                            }
                            break;
                        case Constants.QueryFieldName.Notification.SUBJECT:
                            path = root.get("subject");
                            predicates.add(criteriaBuilder.like(path, "%" + criteria.getValue() + "%"));
                            break;
                        case Constants.QueryFieldName.Notification.CONTENT:
                            path = root.get("content");
                            predicates.add(criteriaBuilder.like(path, "%" + criteria.getValue() + "%"));
                            break;
                        case Constants.QueryFieldName.Notification.READ_STATUS:
                            path = root.get("readStatus").get("id");
                            predicates.add(criteriaBuilder.equal(path, criteria.getValue()));
                            break;
                        case Constants.QueryFieldName.Notification.RECEIVER:
                            path = root.get("toUser").get("id");
                            predicates.add(path.in(((List) criteria.getValue()).toArray()));
                            break;
                    }
                }
                Predicate[] pre = new Predicate[predicates.size()];
                criteriaQuery.where(predicates.toArray(pre));
            }
            return criteriaQuery.getRestriction();
        }
    }
}

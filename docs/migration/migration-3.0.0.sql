--- API changes
---
--- User => UserAccount ("user" is a reserved word in several database dialects)
--- Group => CmnGroup ("group" is a reserved word in SQL, and always having to escape table names gets old very quickly,
---    to say nothing about automatically generated code which sometimes cannot do this properly.)
    
--- Database changes: you should start with a database @ Cinnamon 2.5.1 

alter table users rename ui_language_id to language_id;
alter table objects rename column lifecycle_state_id to state_id;
alter table objects rename column pre_id to predecessor_id;
alter table objects rename column locked_by to locker_id;
alter table groups rename is_user to group_of_one;
alter table formats add column obj_version bigint default 0 NOT NULL;
alter table objtypes add column obj_version bigint default 0 NOT NULL;
alter table messages add column obj_version bigint default 0 NOT NULL;
alter table objects rename column lang_id to language_id;
alter table objects rename column latestbranch to latest_branch;
alter table objects rename column latesthead to latest_head;
alter table relations rename column leftid to left_id;
alter table relations rename column rightid to right_id;
alter table change_triggers rename column trigger_type_id to change_trigger_type_id;
alter table folder_types add column config character varying(10241024) default '<config />' NOT NULL;
alter table objtypes add column config character varying(10241024) default '<config />' NOT NULL;
alter table objects alter column version set default '1';
alter table objects rename column version to cmn_version;
alter table objects rename obj_version to version;
alter table objects rename contentsize to content_size;
alter table objects rename contentpath to content_path;
alter table index_items rename for_sysmeta to for_sys_meta;
alter table index_items rename system_index to systemic;
alter table lifecycle_states rename column lifecycle_state_id to life_cycle_state_for_copy_id;

--- update index_types with new package names
update index_types set indexer_class='cinnamon.index.indexer.BooleanXPathIndexer' where name='xpath.boolean_indexer';
update index_types set indexer_class='cinnamon.index.indexer.DateXPathIndexer' where name='xpath.date_indexer';
update index_types set indexer_class='cinnamon.index.indexer.IntegerXPathIndexer' where name='xpath.integer_indexer';
update index_types set indexer_class='cinnamon.index.indexer.DecimalXPathIndexer' where name='xpath.decimal_indexer';
update index_types set indexer_class='cinnamon.index.indexer.TimeXPathIndexer' where name='xpath.time_indexer';
update index_types set indexer_class='cinnamon.index.indexer.ReverseStringIndexer' where name='xpath.reverse_string_indexer';
update index_types set indexer_class='cinnamon.index.indexer.ReverseCompleteStringIndexer' where name='xpath.reverse_complete_string_indexer';
update index_types set indexer_class='cinnamon.index.indexer.ParentFolderPathIndexer' where name='xpath.parent_folder_path_indexer';
update index_types set indexer_class='cinnamon.index.indexer.CompleteStringIndexer' where name='xpath.complete_string_indexer';
update index_types set indexer_class='cinnamon.index.indexer.DescendingCompleteStringIndexer' where name='xpath.descending_complete_string_indexer';
update index_types set indexer_class='cinnamon.index.indexer.DescendingStringIndexer' where name='xpath.descending_string_indexer';
update index_types set indexer_class='cinnamon.index.indexer.DescendingReverseCompleteStringIndexer' where name='xpath.descending_reverse_complete_string_indexer';
update index_types set indexer_class='cinnamon.index.indexer.DescendingReverseStringIndexer' where name='xpath.descending_reverse_string_indexer';
update index_types set indexer_class='cinnamon.index.indexer.DefaultIndexer' where name='xpath.string_indexer';
update index_types set va_provider_class = 'cinnamon.index.valueAssistance.DefaultProvider';

alter table change_triggers rename column command to action;
alter table change_triggers add column controller character varying(255) not null default 'cinnamon';
update change_trigger_types set trigger_class='cinnamon.trigger.impl.RelationChangeTrigger';

--- test insert:
--- insert into change_triggers values(6,true,0,100,1,'newVersion',true,true,'<config />', 'osd');

update lifecycle_states set state_class='cinnamon.lifecycle.state.NopState' where state_class like '%NopState';
update lifecycle_states set state_class='cinnamon.lifecycle.state.ChangeAclState' where state_class like '%ChangeAclState';

--- update relation resolvers:
update relation_resolvers set class_name = 'cinnamon.relation.resolver.FixedRelationResolver' where name='FixedRelationResolver';
update relation_resolvers set class_name = 'cinnamon.relation.resolver.LatestBranchResolver' where name='LatestBranchResolver';
update relation_resolvers set class_name = 'cinnamon.relation.resolver.LatestHeadResolver' where name='LatestHeadResolver';

update change_trigger_types set trigger_class='cinnamon.trigger.impl.LifecycleStateAuditTrigger' where trigger_class like '%LifecycleStateAuditTrigger';

--- the references to LifeCycle are turned into life_cycle because of camel case:
alter table lifecycle_states rename lifecycle_id to life_cycle_id;
alter table lifecycle_states alter column life_cycle_id drop NOT NULL;

--- add defaultObjectType to Format table:
alter table formats add column default_object_type_id bigint;
alter table formats add constraint defaultObjectType foreign key (default_object_type_id) references objtypes;

--- add store_field value to IndexItem table:
alter table index_items add column store_field boolean not null default false;

alter table metaset_types drop column description;
alter table objtypes drop column description;
alter table relationtypes drop column description;
alter table folder_types drop column description;
alter table acls drop column description;
alter table objtypes alter column config set default '<meta />';
alter table change_trigger_types drop column description ;
alter table customtables rename column jdbcdriver to jdbc_driver;
alter table permissions drop column description;
alter table formats drop column description;
alter table groups drop column description;
alter table links add column version bigint not null default 0;


-- in case your current db is missing some object types:
-- insert into objtypes values(1000,'_object_reference',0,'<config />');
-- insert into objtypes values(1001,'_workflow_template',0,'<config />');
-- insert into objtypes values(1002,'_task_definition',0,'<config />');
-- insert into objtypes values(1003,'_workflow',0,'<config />');
-- insert into objtypes values(1004,'_notification',0,'<config />');

-- optional, recommended if you ever create change_trigger_types manually:
alter table change_trigger_types alter column obj_version set default 0;
alter table change_triggers drop column after_work;
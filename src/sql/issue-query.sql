this will retrieve all the data that represents an issue.

select 
issue.issue_id, attribute.attribute_name, issue_attribute_value.value
from 
scarab_issue issue, scarab_attribute attribute, scarab_issue_attribute_value issue_attribute_value, 
scarab_issue_attribute_vote issue_attribute_vote
where
issue_attribute_vote.issue_id = issue.issue_id
and
issue_attribute_vote.attribute_id = 10
and
issue_attribute_vote.visitor_id = 1
and
issue_attribute_value.issue_id = issue.issue_id
and
attribute.attribute_id = issue_attribute_value.attribute_id
order by 
issue.issue_id, attribute.attribute_id;



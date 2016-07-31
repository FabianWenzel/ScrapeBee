
SELECT count(c.langId) as count, c.langId FROM (

SELECT a.wordId, a.langId
FROM wordinlang a
LEFT OUTER JOIN wordinlang b
ON a.wordId = b.wordId 
AND a.occur > b.occur WHERE 
b.wordId = (SELECT id FROM word WHERE word LIKE 'World') OR 
b.wordId = (SELECT id FROM word WHERE word LIKE 'eye') OR 
b.wordId = (SELECT id FROM word WHERE word LIKE 'hotel') OR 
b.wordId = (SELECT id FROM word WHERE word LIKE 'dem') OR 
b.wordId = (SELECT id FROM word WHERE word LIKE 'drive') 
GROUP BY wordId) c 
GROUP BY c.langId 
ORDER BY count 
DESC limit 1

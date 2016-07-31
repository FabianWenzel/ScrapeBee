CREATE DATABASE `language` /*!40100 DEFAULT CHARACTER SET latin1 */;
CREATE TABLE `language` (
  `id` int(11) NOT NULL,
  `code` varchar(15) DEFAULT NULL,
  `name` varchar(85) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE `url` (
  `id` int(11) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `langId` int(11) DEFAULT NULL,
  `newWords` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE `word` (
  `id` int(11) NOT NULL,
  `word` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE `wordinlang` (
  `id` int(11) NOT NULL,
  `wordId` int(11) DEFAULT NULL,
  `langId` int(11) DEFAULT NULL,
  `occur` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

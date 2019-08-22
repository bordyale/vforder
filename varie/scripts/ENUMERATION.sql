-- phpMyAdmin SQL Dump
-- version 4.6.6deb5
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Creato il: Ago 21, 2019 alle 14:15
-- Versione del server: 5.7.27-0ubuntu0.19.04.1
-- Versione PHP: 7.2.19-0ubuntu0.19.04.2

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ofbiz`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `ENUMERATION`
--

CREATE TABLE `ENUMERATION` (
  `ENUM_ID` varchar(20) COLLATE latin1_general_cs NOT NULL,
  `ENUM_TYPE_ID` varchar(20) COLLATE latin1_general_cs DEFAULT NULL,
  `ENUM_CODE` varchar(60) COLLATE latin1_general_cs DEFAULT NULL,
  `SEQUENCE_ID` varchar(20) COLLATE latin1_general_cs DEFAULT NULL,
  `DESCRIPTION` varchar(255) COLLATE latin1_general_cs DEFAULT NULL,
  `LAST_UPDATED_STAMP` datetime(3) DEFAULT NULL,
  `LAST_UPDATED_TX_STAMP` datetime(3) DEFAULT NULL,
  `CREATED_STAMP` datetime(3) DEFAULT NULL,
  `CREATED_TX_STAMP` datetime(3) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

--
-- Dump dei dati per la tabella `ENUMERATION`
--

INSERT INTO `ENUMERATION` (`ENUM_ID`, `ENUM_TYPE_ID`, `ENUM_CODE`, `SEQUENCE_ID`, `DESCRIPTION`, `LAST_UPDATED_STAMP`, `LAST_UPDATED_TX_STAMP`, `CREATED_STAMP`, `CREATED_TX_STAMP`) VALUES
('CONR_TYPE_FISSO', 'CONTR_DETAIL_TYPE', 'CONR_TYPE_FISSO', '02', 'Tipo Fisso', '2019-07-31 13:27:29.084', '2019-07-31 13:27:29.065', '2019-07-31 13:27:29.084', '2019-07-31 13:27:29.065'),
('CONR_TYPE_PERC', 'CONTR_DETAIL_TYPE', 'CONR_TYPE_PERC', '01', 'Tipo Percentuale', '2019-07-31 13:27:29.084', '2019-07-31 13:27:29.065', '2019-07-31 13:27:29.084', '2019-07-31 13:27:29.065');

--
-- Indici per le tabelle scaricate
--

--
-- Indici per le tabelle `ENUMERATION`
--
ALTER TABLE `ENUMERATION`
  ADD PRIMARY KEY (`ENUM_ID`),
  ADD KEY `ENUM_TO_TYPE` (`ENUM_TYPE_ID`),
  ADD KEY `ENUMERATION_TXSTMP` (`LAST_UPDATED_TX_STAMP`),
  ADD KEY `ENUMERATION_TXCRTS` (`CREATED_TX_STAMP`);

--
-- Limiti per le tabelle scaricate
--

--
-- Limiti per la tabella `ENUMERATION`
--
ALTER TABLE `ENUMERATION`
  ADD CONSTRAINT `ENUM_TO_TYPE` FOREIGN KEY (`ENUM_TYPE_ID`) REFERENCES `ENUMERATION_TYPE` (`ENUM_TYPE_ID`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

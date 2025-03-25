
CREATE TABLE "breadcrumbs" (
	"_id" INTEGER PRIMARY KEY AUTOINCREMENT,
	"jobdetailid" INTEGER,
	"lat" TEXT,
	"long" TEXT,
	"resolution" TEXT,
	"direction" TEXT,
	"speed" TEXT,
	"deliverymode" TEXT,
	"timestamp" INTEGER,
	"timestamplocal" INTEGER,
	"uploaded" INTEGER,
	"uploadbatchid" INTEGER,
	'resolvedaddress' TEXT);

CREATE TABLE "itemvalues" (
	"_id" INTEGER PRIMARY KEY AUTOINCREMENT,
	"itemname" TEXT not null,
	"itemvalue" TEXT);

CREATE TABLE "logins" (
	"_id" INTEGER PRIMARY KEY AUTOINCREMENT,
	"logindate" INTEGER,
	"status" TEXT,
	"uploaded" INTEGER,
	"uploadbatchid" INTEGER);

CREATE TABLE "photos" (
	"_id" INTEGER PRIMARY KEY AUTOINCREMENT,
	"deliveryid" INTEGER,
	"jobdetailid" INTEGER,
	"lat" TEXT,
	"long" TEXT,
	"photonotes" TEXT,
	"photodate" INTEGER,
	"filepath" TEXT,
	"uploaded" INTEGER,
	"fileuploaded" INTEGER,
	"uploadbatchid" INTEGER);

CREATE TABLE "routelist" (
	"_id"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"routeid"	TEXT,
	"jobid"	TEXT,
	"jobdetailid"	INTEGER,
	"interfacetype"	TEXT,
	"scheduletype"	TEXT,
	"city"	TEXT,
	"state"	TEXT,
	"zip"	TEXT,
	"datevalidfrom"	INTEGER,
	"datevalidto"	INTEGER,
	"datevalidfromsoft"	INTEGER,
	"datevalidtosoft"	INTEGER,
	"laststartdate"	INTEGER,
	"lastenddate"	INTEGER,
	"deleted"	INTEGER,
	"deletedconfirmed"	INTEGER,
	"routetype"	TEXT,
	"routefinished"	INTEGER,
	"projected"	INTEGER,
	"downloaded"	INTEGER,
	"lookaheadforward"	INTEGER,
	"lookaheadside"	INTEGER,
	"deliveryforward"	INTEGER,
	"deliveryside"	INTEGER,
	"carriertype"	TEXT);

CREATE TABLE "routelistactivity" (
	"_id" INTEGER PRIMARY KEY AUTOINCREMENT,
	"jobdetailid" INTEGER,
	"startdate" INTEGER,
	"enddate" INTEGER,
	"uploaded" INTEGER,
	"uploadbatchid" INTEGER,
	'routetype' TEXT,
	'routefinished' INTEGER,
	'ttladdresses' INTEGER,
	'ttlremaining' INTEGER);

CREATE TABLE "signatures" (
	"_id"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"deliveryid"	INTEGER,
	"jobdetailid"	INTEGER,
	"lat"	TEXT,
	"long"	TEXT,
	"filepath"	TEXT,
	"uploaded"	INTEGER,
	"fileuploaded"	INTEGER,
	"uploadbatchid"	INTEGER,
	"signaturedate"	INTEGER);

CREATE TABLE "streetsummarylist" (
	"_id" INTEGER PRIMARY KEY AUTOINCREMENT,
	"summaryid" INTEGER,
	"jobdetailid" INTEGER,
	"streetname" TEXT,
	"lat" TEXT,
	"long" TEXT);

CREATE TABLE "uploadlog" (
	"_id" INTEGER PRIMARY KEY AUTOINCREMENT,
	"status" TEXT,
	"uploadtype" TEXT,
	"uploaddatatype" TEXT,
	"timestamp" INTEGER,
	"timestamplocal" INTEGER,
	"numrecordssent" INTEGER,
	"numrecordsconf" INTEGER,
	"numfilessent" INTEGER,
	"numfilesconf" INTEGER,
	"latitude" TEXT,
	"longitude" TEXT,
	"address" TEXT,
	"numnewaddressessent" INTEGER,
	"numnewaddressesconf" INTEGER);

CREATE TABLE "workactivity" (
	"_id"	INTEGER,
	"jobdetailid"	INTEGER,
	"date"	INTEGER,
	"lat"	INTEGER,
	"long"	INTEGER,
	"activityid"	INTEGER,
	"description"	TEXT,
	"uploaded"	INTEGER,
	"uploadbatchid"	INTEGER);

CREATE TABLE "addressdetaillist" (
	"_id"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"jobdetailid"	INTEGER,
	"summaryid"	INTEGER,
	"deliveryid"	INTEGER,
	"recordtype"	TEXT,
	"streetaddress"	TEXT,
	"searchaddress"	TEXT,
	"addressnumber"	TEXT,
	"qty"	INTEGER,
	"lat"	INTEGER,
	"long"	INTEGER,
	"side"	TEXT,
	"latdelivered"	TEXT,
	"longdelivered"	TEXT,
	"latnew"	TEXT,
	"longnew"	TEXT,
	"geoupdatenote"	TEXT,
	"sequence"	INTEGER,
	"sequencenew"	INTEGER,
	"jobtype"	INTEGER,
	"custsvc"	INTEGER,
	"notes"	TEXT,
	"photorequired"	INTEGER,
	"phototaken"	INTEGER,
	"delivered"	INTEGER,
	"deliverydate"	INTEGER,
	"statusupdated"	TEXT,
	"statuscurrent"	TEXT,
	"numrecovered"	INTEGER,
	"numdelivered"	INTEGER,
	"verified"	INTEGER,
	"verifydate"	INTEGER,
	"uploaded"	INTEGER,
	"uploadbatchid"	INTEGER,
	"delinfostatus"	TEXT,
	"delinfoplacement"	TEXT,
	"custid"	TEXT,
	"seqmodenew"	TEXT,
	"wasreconciled"	INTEGER,
	"dndwasprocessed"	INTEGER,
	"listdisplaytime"	INTEGER,
	"signaturetaken"	INTEGER);

CREATE TABLE "addressdetailproducts" (
	"_id"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"productcode"	TEXT,
	"producttype"	TEXT,
	"qty"	INTEGER,
	"deliveryid"	INTEGER,
	"jobdetailid"	INTEGER,
	"scancode"	TEXT,
	"deliverydate"	INTEGER,
	"deliverylatitude"	INTEGER,
	"deliverylongitude"	INTEGER,
	"uploaded"	INTEGER,
	"uploadbatchid"	INTEGER);

CREATE INDEX "adgeoindex" ON "addressdetaillist" (
	"jobdetailid",
	"lat",
	"long");

CREATE UNIQUE INDEX "adjdidsummid" ON "addressdetaillist" (
	"jobdetailid",
	"summaryid",
	"deliveryid");

CREATE INDEX "adup"
ON "addressdetaillist" ("uploaded");

CREATE INDEX "bcjdid"
ON "breadcrumbs" ("jobdetailid" ASC);

CREATE INDEX "bcup"
ON "breadcrumbs" ("uploaded" ASC);

CREATE UNIQUE INDEX "itemvaluesidx"
ON "itemvalues" ("itemname" asc);

CREATE INDEX "lup"
ON "logins" ("uploaded" ASC);

CREATE INDEX "pdelid"
ON "photos" ("deliveryid" ASC);

CREATE INDEX "pjdid"
ON "photos" ("jobdetailid" ASC);

CREATE INDEX "pup"
ON "photos" ("uploaded" ASC);

CREATE INDEX "rlajdid"
ON "routelistactivity" ("jobdetailid" ASC);

CREATE INDEX "rlaup"
ON "routelistactivity" ("uploaded" ASC);

CREATE UNIQUE INDEX "rljdid" ON "routelist" (
	"jobdetailid"	ASC);

CREATE UNIQUE INDEX "ssjdidsummid"
ON "streetsummarylist" ("jobdetailid" asc, "summaryid" ASC);

CREATE INDEX "uniqueAddressProducts" ON "addressdetailproducts" (
	"jobdetailid"	ASC,
	"deliveryid"	ASC,
	"productcode"	ASC);

BEGIN;

INSERT INTO android_metadata ("locale") VALUES ("en_US");

INSERT INTO itemvalues ("itemname", "itemvalue") VALUES ("useMaps", "false");

INSERT INTO itemvalues ("itemname", "itemvalue") VALUES ("useSpeech", "true");

COMMIT;
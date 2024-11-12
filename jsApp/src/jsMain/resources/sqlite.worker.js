importScripts("sqlite3.js");

let db = null;

async function createDatabase(filename) {
  const sqlite3 = await sqlite3InitModule();

  // TODO: Parameterize storage location, and storage type
  db = new sqlite3.oo1.DB(`file:${filename}`, "c");
}

function handleMessage(event) {
  const data = event.data;

  switch (data && data.action) {
    case "exec":
      if (!data["sql"]) {
        throw new Error("exec: Missing query string");
      }

let values = db.exec({ sql: data.sql, bind: data.params, returnValue: "resultRows" })

console.log(JSON.stringify(values))

      return postMessage({
        id: data.id,
        results: { values: values },
      })
    case "begin_transaction":
      return postMessage({
        id: data.id,
        results: db.exec("BEGIN TRANSACTION;"),
      })
    case "end_transaction":
      return postMessage({
        id: data.id,
        results: db.exec("END TRANSACTION;"),
      })
    case "rollback_transaction":
      return postMessage({
        id: data.id,
        results: db.exec("ROLLBACK TRANSACTION;"),
      })
    default:
      throw new Error(`Unsupported action: ${data && data.action}`);
  }
}

function handleError(event, err) {
  return postMessage({
    id: event.data.id,
    error: err,
  });
}

let initializing = false
let pendingEvents = []

if (typeof importScripts === "function") {
  db = null;

  self.onmessage = async (event) => {
    console.log(`onmessage: ${JSON.stringify(event.data)}`)
    if (db === null) {
      if (initializing) {
        pendingEvents.push(event)
      } else {
        try {
          initializing = true
          await createDatabase(event.data)
          for (const pendingEvent of pendingEvents) {
            handleMessage(pendingEvent)
          }
        } catch (err) {
          console.log(`error loading database: err: ${err.message},  data: ${JSON.stringify(event.data)}`)
        }
      }
    } else {
      try {
        handleMessage(event)
      } catch (err) {
        handleError(event, err)
      }
    }
  }
}